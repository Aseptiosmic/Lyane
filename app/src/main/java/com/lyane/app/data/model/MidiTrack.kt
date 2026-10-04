package com.lyane.app.data.model

import java.io.Serializable

data class MidiTrack(
    val index: Int,
    var name: String = "Track $index",
    var channel: Int = 0,
    var isMuted: Boolean = false,
    var isSolo: Boolean = false,
    var volume: Float = 1.0f,
    var pan: Float = 0.0f,
    var instrumentName: String = "Acoustic Grand Piano",
    val notes: MutableList<MidiNote> = mutableListOf()
) : Serializable {

    val noteCount: Int
        get() = notes.size

    val durationUs: Long
        get() = notes.maxOfOrNull { it.endTimeUs } ?: 0L

    fun getActiveNotesAt(timeUs: Long): List<MidiNote> {
        if (isMuted) return emptyList()
        return notes.filter { timeUs in it.startTimeUs..it.endTimeUs }
    }
}
