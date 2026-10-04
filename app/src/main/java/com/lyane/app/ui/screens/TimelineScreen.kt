package com.lyane.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    // Collected purely to force recomposition when a track's mute/solo state is toggled
    // in place (see MainViewModel.bumpEditVersion()).
    val editVersion by viewModel.editVersion.collectAsState()

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

        val tracks = sequence?.tracks.orEmpty()

        // Tracks List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("MIDI Tracks (${tracks.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (tracks.isEmpty()) {
                item {
                    Text("No sequence loaded.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }

            items(tracks) { track ->
                val trackIndex = tracks.indexOf(track)
                TrackLayerCard(
                    title = track.name,
                    subtitle = "${track.noteCount} Notes • ${track.instrumentName}",
                    icon = Icons.Default.MusicNote,
                    accentColor = LyaneCyan,
                    isMuted = track.isMuted,
                    isSolo = track.isSolo,
                    onToggleMute = { viewModel.toggleTrackMute(trackIndex) },
                    onToggleSolo = { viewModel.toggleTrackSolo(trackIndex) }
                )
            }

            // External Audio Layer — disclosed as not yet available rather than faked:
            // AudioPlayerSync exists in the engine but is not wired to project loading/UI yet.
            item {
                Spacer(modifier = Modifier.height(8.dp))
                UnavailableLayerCard(
                    title = "External Audio Layer",
                    subtitle = "Sync an external backing-track recording to the MIDI — coming in a future update",
                    icon = Icons.Default.Audiotrack,
                    accentColor = LyanePurple
                )
            }

            // Camera / Video Background Overlay — disclosed as not yet available: there is no
            // live camera feed implemented yet to blend behind the falling notes, so the control
            // is shown disabled instead of silently doing nothing.
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LyaneDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextMuted)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Camera / Video Background Overlay", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Blend real piano hands with falling notes — not available in this version", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Surface(color = LyaneBorder, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "SOON",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Background Opacity", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Slider(
                            value = 0f,
                            onValueChange = {},
                            enabled = false,
                            colors = SliderDefaults.colors(disabledThumbColor = TextMuted, disabledActiveTrackColor = TextMuted)
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
    isSolo: Boolean,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LyaneDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSolo) LyaneAurora else LyaneBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isMuted) TextMuted else accentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = if (isMuted) TextMuted else TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = isSolo,
                onClick = onToggleSolo,
                label = { Text("S") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LyaneAurora,
                    selectedLabelColor = LyaneBlack
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            FilterChip(
                selected = isMuted,
                onClick = onToggleMute,
                label = { Text("M") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF87171),
                    selectedLabelColor = LyaneBlack
                )
            )
        }
    }
}

@Composable
private fun UnavailableLayerCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
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
            Icon(icon, contentDescription = null, tint = TextMuted)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Surface(color = LyaneBorder, shape = RoundedCornerShape(6.dp)) {
                Text(
                    "SOON",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
