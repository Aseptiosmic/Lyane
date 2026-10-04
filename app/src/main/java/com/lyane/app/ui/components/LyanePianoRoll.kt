package com.lyane.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lyane.app.data.model.MidiNote
import com.lyane.app.data.model.MidiSequence
import com.lyane.app.ui.theme.*

@Composable
fun LyanePianoRoll(
    sequence: MidiSequence?,
    currentPositionUs: Long,
    trackIndex: Int,
    onNoteSelected: (MidiNote) -> Unit,
    onNoteMoved: (MidiNote, newPitch: Int, newTimeUs: Long) -> Unit,
    onNoteResized: (MidiNote, newDurationUs: Long) -> Unit,
    onAddNote: (pitch: Int, timeUs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sequence == null) return

    val track = sequence.tracks.getOrNull(trackIndex) ?: sequence.tracks.firstOrNull()
    val notes = track?.notes ?: emptyList()

    val totalDurationUs = maxOf(10_000_000L, sequence.durationUs + 2_000_000L)
    val minPitch = 21
    val maxPitch = 108
    val totalPitches = maxPitch - minPitch + 1

    var selectedNote by remember { mutableStateOf<MidiNote?>(null) }

    Box(modifier = modifier.background(LyaneBlack)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val w = size.width
                        val h = size.height

                        val pitchHeight = h / totalPitches
                        val timePerPixel = totalDurationUs / w

                        val clickedPitch = maxPitch - (offset.y / pitchHeight).toInt()
                        val clickedTimeUs = (offset.x * timePerPixel).toLong()

                        // Check if tapped an existing note
                        val hit = notes.find {
                            it.pitch == clickedPitch && clickedTimeUs in it.startTimeUs..it.endTimeUs
                        }

                        if (hit != null) {
                            selectedNote = hit
                            onNoteSelected(hit)
                        } else {
                            // Add note at location
                            onAddNote(clickedPitch, clickedTimeUs)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val pitchHeight = h / totalPitches
            val timePerPixel = totalDurationUs / w

            // 1. Draw Piano Roll Key Rows
            for (p in minPitch..maxPitch) {
                val rowY = (maxPitch - p) * pitchHeight
                val isBlack = (p % 12) in listOf(1, 3, 6, 8, 10)
                drawRect(
                    color = if (isBlack) Color(0xFF0F111A) else Color(0xFF141824),
                    topLeft = Offset(0f, rowY),
                    size = Size(w, pitchHeight)
                )
                // Divider line
                drawLine(
                    color = Color(0xFF1E2436),
                    start = Offset(0f, rowY),
                    end = Offset(w, rowY),
                    strokeWidth = 1f
                )
            }

            // 2. Draw Measure / Beat Vertical Grid lines
            val usPerMeasure = 2_000_000L // ~120 BPM 4/4 measure
            val numMeasures = (totalDurationUs / usPerMeasure).toInt() + 1
            for (m in 0 until numMeasures) {
                val lineX = (m * usPerMeasure) / timePerPixel
                drawLine(
                    color = Color(0xFF2A344D),
                    start = Offset(lineX, 0f),
                    end = Offset(lineX, h),
                    strokeWidth = 1.5f
                )
            }

            // 3. Draw Notes
            for (note in notes) {
                val noteX = (note.startTimeUs / timePerPixel).toFloat()
                val noteW = maxOf(4f, (note.durationUs / timePerPixel).toFloat())
                val noteY = (maxPitch - note.pitch) * pitchHeight

                val isSel = (note == selectedNote)
                val baseColor = if (isSel) LyaneMagenta else LyaneCyan

                // Note rectangle
                drawRoundRect(
                    color = baseColor,
                    topLeft = Offset(noteX, noteY + 1f),
                    size = Size(noteW, pitchHeight - 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )

                // Velocity line inside note
                val velRatio = note.velocity / 127f
                drawRect(
                    color = Color.White.copy(alpha = 0.4f),
                    topLeft = Offset(noteX, noteY + (pitchHeight - 2f) * (1f - velRatio)),
                    size = Size(noteW, (pitchHeight - 2f) * velRatio)
                )
            }

            // 4. Draw Playhead Scrubber
            val playheadX = (currentPositionUs / timePerPixel).toFloat()
            drawLine(
                color = LyaneCyanGlow,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, h),
                strokeWidth = 2.5f
            )
            drawCircle(
                color = LyaneCyan,
                radius = 6f,
                center = Offset(playheadX, 6f)
            )
        }
    }
}
