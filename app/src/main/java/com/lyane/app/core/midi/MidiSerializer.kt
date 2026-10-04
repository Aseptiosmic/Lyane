package com.lyane.app.core.midi

import com.lyane.app.data.model.MidiSequence
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MidiSerializer {

    fun serialize(sequence: MidiSequence): ByteArray {
        val out = ByteArrayOutputStream()
        write(sequence, out)
        return out.toByteArray()
    }

    fun write(sequence: MidiSequence, out: OutputStream) {
        val ppq = sequence.ppq
        val numTracks = maxOf(1, sequence.tracks.size)

        // Write MThd header
        val header = ByteBuffer.allocate(14).order(ByteOrder.BIG_ENDIAN)
        header.put("MThd".toByteArray())
        header.putInt(6) // length
        header.putShort(1.toShort()) // Format 1
        header.putShort(numTracks.toShort())
        header.putShort(ppq.toShort())
        out.write(header.array())

        val tempo = sequence.tempoEvents.firstOrNull()?.usPerQuarter ?: 500_000L

        fun microsToTicks(us: Long): Long {
            return (us * ppq) / tempo
        }

        for (track in sequence.tracks) {
            val trackBytes = ByteArrayOutputStream()
            var currentTick = 0L

            // Track Name Meta Event
            val nameBytes = track.name.toByteArray()
            writeVarLen(0L, trackBytes)
            trackBytes.write(0xFF)
            trackBytes.write(0x03)
            writeVarLen(nameBytes.size.toLong(), trackBytes)
            trackBytes.write(nameBytes)

            // Set Tempo Meta Event — written into the first track actually present in the
            // output file. Using `track.index == 0` here was unreliable: MidiParser only keeps
            // tracks that contain notes, so an imported file's original empty "conductor" track
            // (index 0, tempo-only) is routinely dropped, meaning no track would ever match
            // `index == 0` and the tempo would silently be omitted from re-exported files.
            if (track === sequence.tracks.firstOrNull()) {
                writeVarLen(0L, trackBytes)
                trackBytes.write(0xFF)
                trackBytes.write(0x51)
                trackBytes.write(0x03)
                trackBytes.write((tempo shr 16).toInt() and 0xFF)
                trackBytes.write((tempo shr 8).toInt() and 0xFF)
                trackBytes.write(tempo.toInt() and 0xFF)
            }

            // Gather all NoteOn and NoteOff events
            data class Event(val tick: Long, val type: Int, val pitch: Int, val vel: Int, val ch: Int)
            val events = mutableListOf<Event>()

            for (note in track.notes) {
                val onTick = microsToTicks(note.startTimeUs)
                val offTick = microsToTicks(note.endTimeUs)
                events.add(Event(onTick, 0x90, note.pitch, note.velocity, note.channel))
                events.add(Event(offTick, 0x80, note.pitch, 0, note.channel))
            }
            events.sortBy { it.tick }

            for (evt in events) {
                val delta = maxOf(0L, evt.tick - currentTick)
                currentTick = evt.tick
                writeVarLen(delta, trackBytes)
                val status = evt.type or (evt.ch and 0x0F)
                trackBytes.write(status)
                trackBytes.write(evt.pitch and 0x7F)
                trackBytes.write(evt.vel and 0x7F)
            }

            // End of Track Meta Event
            writeVarLen(0L, trackBytes)
            trackBytes.write(0xFF)
            trackBytes.write(0x2F)
            trackBytes.write(0x00)

            val trackData = trackBytes.toByteArray()
            val trackHeader = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
            trackHeader.put("MTrk".toByteArray())
            trackHeader.putInt(trackData.size)
            out.write(trackHeader.array())
            out.write(trackData)
        }
    }

    private fun writeVarLen(value: Long, out: OutputStream) {
        var buffer = value and 0x7F
        var v = value ushr 7
        val bytes = mutableListOf<Int>()
        bytes.add(buffer.toInt())

        while (v > 0) {
            buffer = (v and 0x7F) or 0x80
            bytes.add(0, buffer.toInt())
            v = v ushr 7
        }

        for (b in bytes) {
            out.write(b)
        }
    }
}
