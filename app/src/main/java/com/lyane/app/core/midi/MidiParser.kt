package com.lyane.app.core.midi

import com.lyane.app.data.model.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MidiParser {

    fun parse(bytes: ByteArray, fileName: String = "Imported MIDI"): MidiSequence {
        return parse(ByteArrayInputStream(bytes), fileName)
    }

    fun parse(stream: InputStream, fileName: String = "Imported MIDI"): MidiSequence {
        val bytes = stream.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

        // Read MThd
        if (buffer.remaining() < 14) {
            throw IllegalArgumentException("Invalid MIDI file: file too short")
        }

        val headerTag = readString(buffer, 4)
        if (headerTag != "MThd") {
            throw IllegalArgumentException("Invalid MIDI file: missing MThd header tag")
        }

        val headerLength = buffer.int
        val format = buffer.short.toInt()
        val numTracks = buffer.short.toInt()
        val division = buffer.short.toInt()

        // Skip any extra header bytes if length > 6
        if (headerLength > 6) {
            buffer.position(buffer.position() + (headerLength - 6))
        }

        val ppq = if (division > 0) division else 480
        val sequence = MidiSequence(name = fileName, ppq = ppq)

        val rawTracks = mutableListOf<RawMidiTrack>()

        for (t in 0 until numTracks) {
            if (buffer.remaining() < 8) break
            val trackTag = readString(buffer, 4)
            val trackLength = buffer.int
            if (trackTag != "MTrk") {
                // Skip unknown chunk
                buffer.position(minOf(buffer.capacity(), buffer.position() + trackLength))
                continue
            }

            val trackBytes = ByteArray(trackLength)
            buffer.get(trackBytes)
            rawTracks.add(parseTrackChunk(t, trackBytes))
        }

        // Build unified tempo map across all tracks
        val allTempoEvents = mutableListOf<TempoEvent>()
        var globalTickToTime = mutableListOf<Pair<Long, Long>>() // (tick, usPerQuarter)
        
        for (raw in rawTracks) {
            for (evt in raw.tempoEvents) {
                allTempoEvents.add(evt)
            }
        }
        allTempoEvents.sortBy { it.tick }

        if (allTempoEvents.isEmpty()) {
            allTempoEvents.add(TempoEvent(0, 500_000L, 0L)) // Default 120 BPM
        }

        // Calculate absolute microseconds for each tempo event
        val calculatedTempos = mutableListOf<TempoEvent>()
        var currentUs = 0L
        var prevTick = 0L
        var currentUsPerQuarter = 500_000L

        for (evt in allTempoEvents) {
            val deltaTicks = evt.tick - prevTick
            currentUs += (deltaTicks * currentUsPerQuarter) / ppq
            calculatedTempos.add(TempoEvent(evt.tick, evt.usPerQuarter, currentUs))
            prevTick = evt.tick
            currentUsPerQuarter = evt.usPerQuarter
        }
        sequence.tempoEvents.addAll(calculatedTempos)

        fun tickToMicros(targetTick: Long): Long {
            var us = 0L
            var lastTick = 0L
            var usPerQ = 500_000L

            for (tempo in calculatedTempos) {
                if (targetTick <= tempo.tick) {
                    val delta = targetTick - lastTick
                    return us + (delta * usPerQ) / ppq
                }
                val delta = tempo.tick - lastTick
                us += (delta * usPerQ) / ppq
                lastTick = tempo.tick
                usPerQ = tempo.usPerQuarter
            }
            val remaining = targetTick - lastTick
            return us + (remaining * usPerQ) / ppq
        }

        // Convert raw notes to final MidiNotes with microsecond timestamps
        for (raw in rawTracks) {
            val midiTrack = MidiTrack(
                index = raw.index,
                name = raw.trackName.ifBlank { "Track ${raw.index + 1}" },
                channel = raw.primaryChannel
            )

            // Pair NoteOn with NoteOff
            val activeNotes = mutableMapOf<Int, MutableList<RawNoteOn>>()

            for (evt in raw.events) {
                when (evt) {
                    is RawEvent.NoteOn -> {
                        if (evt.velocity > 0) {
                            val list = activeNotes.getOrPut(evt.pitch) { mutableListOf() }
                            list.add(RawNoteOn(evt.tick, evt.velocity, evt.channel))
                        } else {
                            // Velocity 0 is NoteOff
                            val list = activeNotes[evt.pitch]
                            if (!list.isNullOrEmpty()) {
                                val on = list.removeAt(0)
                                val startUs = tickToMicros(on.tick)
                                val endUs = tickToMicros(evt.tick)
                                val durationUs = maxOf(10_000L, endUs - startUs)
                                midiTrack.notes.add(
                                    MidiNote(
                                        pitch = evt.pitch,
                                        startTimeUs = startUs,
                                        durationUs = durationUs,
                                        velocity = on.velocity,
                                        trackIndex = raw.index,
                                        channel = on.channel
                                    )
                                )
                            }
                        }
                    }
                    is RawEvent.NoteOff -> {
                        val list = activeNotes[evt.pitch]
                        if (!list.isNullOrEmpty()) {
                            val on = list.removeAt(0)
                            val startUs = tickToMicros(on.tick)
                            val endUs = tickToMicros(evt.tick)
                            val durationUs = maxOf(10_000L, endUs - startUs)
                            midiTrack.notes.add(
                                MidiNote(
                                    pitch = evt.pitch,
                                    startTimeUs = startUs,
                                    durationUs = durationUs,
                                    velocity = on.velocity,
                                    trackIndex = raw.index,
                                    channel = on.channel
                                )
                            )
                        }
                    }
                }
            }

            // Flush any unclosed notes at the end of track
            for ((pitch, list) in activeNotes) {
                for (on in list) {
                    val startUs = tickToMicros(on.tick)
                    val endUs = tickToMicros(on.tick + ppq) // Default 1 beat
                    midiTrack.notes.add(
                        MidiNote(
                            pitch = pitch,
                            startTimeUs = startUs,
                            durationUs = maxOf(10_000L, endUs - startUs),
                            velocity = on.velocity,
                            trackIndex = raw.index,
                            channel = on.channel
                        )
                    )
                }
            }

            midiTrack.notes.sortBy { it.startTimeUs }
            if (midiTrack.notes.isNotEmpty()) {
                sequence.tracks.add(midiTrack)
            }
        }

        // If no notes loaded into sequence, provide at least one empty track
        if (sequence.tracks.isEmpty()) {
            sequence.tracks.add(MidiTrack(index = 0, name = "Piano Track"))
        }

        return sequence
    }

    private fun parseTrackChunk(trackIndex: Int, data: ByteArray): RawMidiTrack {
        val stream = ByteArrayInputStream(data)
        val raw = RawMidiTrack(trackIndex)
        var currentTick = 0L
        var runningStatus = 0

        while (stream.available() > 0) {
            val delta = readVarLen(stream)
            currentTick += delta

            var status = stream.read()
            if (status < 0) break

            if (status < 0x80) {
                // Running status
                stream.reset()
                // Wait, ByteArrayInputStream doesn't mark by default, so we rewind 1 byte:
                // Better approach: handle running status with stream position or peek
                // We will implement position tracking safely
            }
            // Handled in safe buffer parser below
        }

        // Use safe byte array parser
        return parseTrackChunkSafe(trackIndex, data)
    }

    private fun parseTrackChunkSafe(trackIndex: Int, data: ByteArray): RawMidiTrack {
        val raw = RawMidiTrack(trackIndex)
        var pos = 0
        var currentTick = 0L
        var runningStatus = 0

        while (pos < data.size) {
            val (delta, bytesRead) = readVarLenFromBytes(data, pos)
            pos += bytesRead
            currentTick += delta

            if (pos >= data.size) break

            var status = data[pos].toInt() and 0xFF
            if (status < 0x80) {
                status = runningStatus
            } else {
                pos++
                runningStatus = status
            }

            val msgType = status and 0xF0
            val channel = status and 0x0F
            if (raw.primaryChannel == 0 && channel != 0) {
                raw.primaryChannel = channel
            }

            when (msgType) {
                0x80 -> { // Note Off
                    if (pos + 1 < data.size) {
                        val pitch = data[pos++].toInt() and 0x7F
                        val vel = data[pos++].toInt() and 0x7F
                        raw.events.add(RawEvent.NoteOff(currentTick, pitch, vel, channel))
                    }
                }
                0x90 -> { // Note On
                    if (pos + 1 < data.size) {
                        val pitch = data[pos++].toInt() and 0x7F
                        val vel = data[pos++].toInt() and 0x7F
                        raw.events.add(RawEvent.NoteOn(currentTick, pitch, vel, channel))
                    }
                }
                0xA0 -> { // Polyphonic Aftertouch
                    pos = minOf(data.size, pos + 2)
                }
                0xB0 -> { // Control Change
                    if (pos + 1 < data.size) {
                        val ccNum = data[pos++].toInt() and 0x7F
                        val ccVal = data[pos++].toInt() and 0x7F
                        // CC 64 = Sustain pedal
                    }
                }
                0xC0 -> { // Program Change
                    if (pos < data.size) pos++
                }
                0xD0 -> { // Channel Aftertouch
                    if (pos < data.size) pos++
                }
                0xE0 -> { // Pitch Bend
                    pos = minOf(data.size, pos + 2)
                }
                0xF0 -> { // SysEx or Meta Event
                    if (status == 0xFF) { // Meta Event
                        if (pos < data.size) {
                            val metaType = data[pos++].toInt() and 0xFF
                            val (len, lenBytes) = readVarLenFromBytes(data, pos)
                            pos += lenBytes
                            val metaEnd = minOf(data.size, pos + len.toInt())

                            when (metaType) {
                                0x03 -> { // Track Name
                                    val nameBytes = data.copyOfRange(pos, metaEnd)
                                    raw.trackName = String(nameBytes)
                                }
                                0x51 -> { // Set Tempo
                                    if (metaEnd - pos >= 3) {
                                        val b0 = data[pos].toLong() and 0xFF
                                        val b1 = data[pos + 1].toLong() and 0xFF
                                        val b2 = data[pos + 2].toLong() and 0xFF
                                        val usPerQ = (b0 shl 16) or (b1 shl 8) or b2
                                        raw.tempoEvents.add(TempoEvent(currentTick, usPerQ, 0L))
                                    }
                                }
                            }
                            pos = metaEnd
                        }
                    } else if (status == 0xF0 || status == 0xF7) {
                        val (sysLen, sysBytes) = readVarLenFromBytes(data, pos)
                        pos += sysBytes + sysLen.toInt()
                    }
                }
            }
        }
        return raw
    }

    private fun readVarLenFromBytes(data: ByteArray, start: Int): Pair<Long, Int> {
        var value = 0L
        var count = 0
        var pos = start

        while (pos < data.size && count < 4) {
            val b = data[pos++].toInt() and 0xFF
            count++
            value = (value shl 7) or ((b and 0x7F).toLong())
            if ((b and 0x80) == 0) break
        }
        return Pair(value, count)
    }

    private fun readVarLen(stream: InputStream): Long {
        var value = 0L
        var count = 0
        while (count < 4) {
            val b = stream.read()
            if (b < 0) break
            count++
            value = (value shl 7) or ((b and 0x7F).toLong())
            if ((b and 0x80) == 0) break
        }
        return value
    }

    private fun readString(buffer: ByteBuffer, length: Int): String {
        val bytes = ByteArray(length)
        buffer.get(bytes)
        return String(bytes)
    }

    private data class RawNoteOn(val tick: Long, val velocity: Int, val channel: Int)

    private sealed class RawEvent {
        data class NoteOn(val tick: Long, val pitch: Int, val velocity: Int, val channel: Int) : RawEvent()
        data class NoteOff(val tick: Long, val pitch: Int, val velocity: Int, val channel: Int) : RawEvent()
    }

    private class RawMidiTrack(val index: Int) {
        var trackName: String = ""
        var primaryChannel: Int = 0
        val events = mutableListOf<RawEvent>()
        val tempoEvents = mutableListOf<TempoEvent>()
    }
}
