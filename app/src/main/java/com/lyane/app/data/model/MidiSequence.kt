package com.lyane.app.data.model

import java.io.Serializable

data class TempoEvent(
    val tick: Long,
    val usPerQuarter: Long,
    val timeUs: Long
) : Serializable {
    val bpm: Double
        get() = 60_000_000.0 / usPerQuarter
}

data class TimeSignatureEvent(
    val tick: Long,
    val numerator: Int,
    val denominator: Int,
    val timeUs: Long
) : Serializable

data class KeySignatureEvent(
    val tick: Long,
    val key: Int,      // negative for flats, positive for sharps (-7..7)
    val isMinor: Boolean,
    val timeUs: Long
) : Serializable

data class MidiSequence(
    var name: String = "Untitled Sequence",
    var ppq: Int = 480, // Pulses (ticks) per quarter note
    val tracks: MutableList<MidiTrack> = mutableListOf(),
    val tempoEvents: MutableList<TempoEvent> = mutableListOf(),
    val timeSignatures: MutableList<TimeSignatureEvent> = mutableListOf(),
    val keySignatures: MutableList<KeySignatureEvent> = mutableListOf()
) : Serializable {

    val durationUs: Long
        get() = tracks.maxOfOrNull { it.durationUs } ?: 0L

    val allNotes: List<MidiNote>
        get() = tracks.flatMap { it.notes }

    val totalNoteCount: Int
        get() = tracks.sumOf { it.noteCount }

    val initialBpm: Double
        get() = tempoEvents.firstOrNull()?.bpm ?: 120.0

    fun getNotesInRange(startUs: Long, endUs: Long): List<MidiNote> {
        val list = mutableListOf<MidiNote>()
        for (track in tracks) {
            if (track.isMuted) continue
            for (note in track.notes) {
                if (note.endTimeUs >= startUs && note.startTimeUs <= endUs) {
                    list.add(note)
                }
            }
        }
        return list
    }

    fun getActiveNotesAt(timeUs: Long): List<MidiNote> {
        val hasSolo = tracks.any { it.isSolo }
        val activeTracks = if (hasSolo) tracks.filter { it.isSolo } else tracks.filter { !it.isMuted }
        return activeTracks.flatMap { it.getActiveNotesAt(timeUs) }
    }
}
