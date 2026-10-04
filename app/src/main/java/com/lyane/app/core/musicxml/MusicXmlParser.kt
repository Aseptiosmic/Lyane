package com.lyane.app.core.musicxml

import com.lyane.app.data.model.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.StringReader

class MusicXmlParser {

    fun parse(stream: InputStream, title: String = "Imported MusicXML"): MidiSequence {
        val xmlText = stream.bufferedReader().use { it.readText() }
        return parseString(xmlText, title)
    }

    fun parseString(xmlContent: String, title: String = "Imported MusicXML"): MidiSequence {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        val sequence = MidiSequence(name = title, ppq = 480)
        var currentTrack = MidiTrack(index = 0, name = "Piano Part")

        var divisions = 4
        var currentBpm = 120.0
        var currentMicroseconds = 0L
        var measureMicroseconds = 0L

        var inPitch = false
        var step = ""
        var alter = 0
        var octave = 4
        var isRest = false
        var isChord = false
        var durationTicks = 0
        var previousNoteStartUs = 0L

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "work-title", "movement-title" -> {
                            sequence.name = parser.nextText()
                        }
                        "part-name" -> {
                            currentTrack.name = parser.nextText()
                        }
                        "divisions" -> {
                            divisions = parser.nextText().toIntOrNull() ?: 4
                        }
                        "sound" -> {
                            val tempoAttr = parser.getAttributeValue(null, "tempo")
                            if (tempoAttr != null) {
                                currentBpm = tempoAttr.toDoubleOrNull() ?: 120.0
                                val usPerQ = (60_000_000.0 / currentBpm).toLong()
                                sequence.tempoEvents.add(TempoEvent(0L, usPerQ, currentMicroseconds))
                            }
                        }
                        "measure" -> {
                            measureMicroseconds = currentMicroseconds
                        }
                        "note" -> {
                            isRest = false
                            isChord = false
                            step = ""
                            alter = 0
                            octave = 4
                            durationTicks = 0
                        }
                        "rest" -> {
                            isRest = true
                        }
                        "chord" -> {
                            isChord = true
                        }
                        "pitch" -> {
                            inPitch = true
                        }
                        "step" -> {
                            if (inPitch) step = parser.nextText()
                        }
                        "alter" -> {
                            if (inPitch) alter = parser.nextText().toIntOrNull() ?: 0
                        }
                        "octave" -> {
                            if (inPitch) octave = parser.nextText().toIntOrNull() ?: 4
                        }
                        "duration" -> {
                            durationTicks = parser.nextText().toIntOrNull() ?: divisions
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name.lowercase()) {
                        "pitch" -> {
                            inPitch = false
                        }
                        "note" -> {
                            val usPerQuarter = (60_000_000.0 / currentBpm).toLong()
                            val durationUs = ((durationTicks.toDouble() / divisions) * usPerQuarter).toLong()

                            val noteStartUs = if (isChord) previousNoteStartUs else currentMicroseconds

                            if (!isRest && step.isNotBlank()) {
                                val pitch = stepOctaveToMidi(step, alter, octave)
                                currentTrack.notes.add(
                                    MidiNote(
                                        pitch = pitch,
                                        startTimeUs = noteStartUs,
                                        durationUs = maxOf(20_000L, durationUs),
                                        velocity = 80,
                                        trackIndex = 0,
                                        channel = 0
                                    )
                                )
                            }

                            if (!isChord) {
                                previousNoteStartUs = noteStartUs
                                currentMicroseconds += durationUs
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        if (sequence.tempoEvents.isEmpty()) {
            val usPerQ = (60_000_000.0 / currentBpm).toLong()
            sequence.tempoEvents.add(TempoEvent(0L, usPerQ, 0L))
        }

        currentTrack.notes.sortBy { it.startTimeUs }
        sequence.tracks.add(currentTrack)

        return sequence
    }

    private fun stepOctaveToMidi(step: String, alter: Int, octave: Int): Int {
        val basePitch = when (step.uppercase()) {
            "C" -> 0
            "D" -> 2
            "E" -> 4
            "F" -> 5
            "G" -> 7
            "A" -> 9
            "B" -> 11
            else -> 0
        }
        val midi = (octave + 1) * 12 + basePitch + alter
        return midi.coerceIn(21, 108)
    }
}
