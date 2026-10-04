package com.lyane.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.components.LyaneTimelineBar
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun TimelineScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val sequence by viewModel.currentSequence.collectAsState()
    val positionUs by viewModel.positionUs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var backgroundOpacity by remember { mutableStateOf(0.7f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Multi-Track Timeline & Sync", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("MIDI, Audio & Camera layers", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tracks List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Track 1: MIDI Notes
            item {
                TrackLayerCard(
                    title = "MIDI Performance Layer",
                    subtitle = "${sequence?.tracks?.size ?: 1} Tracks • ${sequence?.totalNoteCount ?: 0} Notes",
                    icon = Icons.Default.MusicNote,
                    accentColor = LyaneCyan,
                    isMuted = false,
                    onToggleMute = {}
                )
            }

            // Track 2: Audio Track Sync
            item {
                TrackLayerCard(
                    title = "External Audio Layer",
                    subtitle = "Acoustic Piano Synthesizer / Audio Sync",
                    icon = Icons.Default.Audiotrack,
                    accentColor = LyanePurple,
                    isMuted = false,
                    onToggleMute = {}
                )
            }

            // Track 3: Camera / Video Background Overlay
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LyaneDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LyaneAurora)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Camera / Video Background Overlay", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Blend real piano hands with falling notes", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Background Opacity: ${(backgroundOpacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Slider(
                            value = backgroundOpacity,
                            onValueChange = { backgroundOpacity = it },
                            colors = SliderDefaults.colors(thumbColor = LyaneAurora, activeTrackColor = LyaneAurora)
                        )
                    }
                }
            }
        }

        // Bottom Scrubber Bar
        LyaneTimelineBar(
            positionUs = positionUs,
            durationUs = sequence?.durationUs ?: 1L,
            isPlaying = isPlaying,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onStop = { viewModel.stop() },
            onSeekProgress = { viewModel.seekProgress(it) }
        )
    }
}

@Composable
fun TrackLayerCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isMuted: Boolean,
    onToggleMute: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LyaneDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
