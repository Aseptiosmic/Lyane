package com.lyane.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyane.app.data.model.MidiNote
import com.lyane.app.ui.components.LyanePianoRoll
import com.lyane.app.ui.components.LyaneTimelineBar
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val sequence by viewModel.currentSequence.collectAsState()
    val positionUs by viewModel.positionUs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var selectedTrackIndex by remember { mutableStateOf(0) }
    var selectedNote by remember { mutableStateOf<MidiNote?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("MIDI Piano Roll Editor", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(sequence?.name ?: "No Track", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            // Quantize & Undo Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val seq = sequence
                        if (seq != null) {
                            viewModel.editorEngine.quantizeTrack(seq, selectedTrackIndex)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LyaneCardSurface, contentColor = LyaneCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quantize 1/16")
                }

                IconButton(
                    onClick = {
                        val seq = sequence
                        if (seq != null) viewModel.editorEngine.undo(seq)
                    }
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = TextSecondary)
                }
            }
        }

        // Interactive Piano Roll Canvas
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LyanePianoRoll(
                sequence = sequence,
                currentPositionUs = positionUs,
                trackIndex = selectedTrackIndex,
                onNoteSelected = { selectedNote = it },
                onNoteMoved = { note, newPitch, newTime ->
                    val seq = sequence
                    if (seq != null) viewModel.editorEngine.moveNote(seq, note, newPitch, newTime)
                },
                onNoteResized = { note, newDur ->
                    val seq = sequence
                    if (seq != null) viewModel.editorEngine.resizeNote(seq, note, newDur)
                },
                onAddNote = { pitch, time ->
                    val seq = sequence
                    if (seq != null) {
                        viewModel.editorEngine.addNote(seq, selectedTrackIndex, pitch, time, 500_000L)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Note Inspector / Editor Bar (when a note is selected)
        if (selectedNote != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LyaneDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pitch: ${selectedNote?.pitchName} (MIDI ${selectedNote?.pitch}) | Vel: ${selectedNote?.velocity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LyaneCyan
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val seq = sequence
                                val n = selectedNote
                                if (seq != null && n != null) {
                                    viewModel.editorEngine.deleteNote(seq, n)
                                    selectedNote = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF450A0A), contentColor = Color(0xFFF87171)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }

        // Timeline Bar
        LyaneTimelineBar(
            positionUs = positionUs,
            durationUs = sequence?.durationUs ?: 1L,
            isPlaying = isPlaying,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onStop = { viewModel.stop() },
            onSeekProgress = { viewModel.seekProgress(it) },
            modifier = Modifier.padding(16.dp)
        )
    }
}
