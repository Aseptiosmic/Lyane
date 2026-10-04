package com.lyane.app.data.model

import java.io.Serializable

data class MidiNote(
    var id: Long = System.nanoTime(),
    var pitch: Int,             // MIDI note number 21..108 (A0 to C8 on 88-key piano)
    var startTimeUs: Long,      // Note on time in microseconds
    var durationUs: Long,       // Duration in microseconds
    var velocity: Int = 80,     // 1..127
    var trackIndex: Int = 0,
    var channel: Int = 0,
    var isSelected: Boolean = false,
    var colorHex: String? = null
) : Serializable {

    val endTimeUs: Long
        get() = startTimeUs + durationUs

    val pitchName: String
        get() {
            val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val octave = (pitch / 12) - 1
            val noteIndex = pitch % 12
            return "${noteNames[noteIndex]}$octave"
        }

    val pitchClass: Int
        get() = pitch % 12

    val isBlackKey: Boolean
        get() {
            val pc = pitchClass
            return pc == 1 || pc == 3 || pc == 6 || pc == 8 || pc == 10
        }
}
