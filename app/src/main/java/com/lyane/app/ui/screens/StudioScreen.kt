package com.lyane.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyane.app.data.model.NoteStyle
import com.lyane.app.ui.components.LyaneTimelineBar
import com.lyane.app.ui.components.LyaneVisualizerView
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun StudioScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToPresets: () -> Unit
) {
    val sequence by viewModel.currentSequence.collectAsState()
    val positionUs by viewModel.positionUs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val visualConfig by viewModel.visualConfig.collectAsState()
    val cameraConfig by viewModel.cameraConfig.collectAsState()
    val particleConfig by viewModel.particleConfig.collectAsState()
    val lightingConfig by viewModel.lightingConfig.collectAsState()

    var showControlsDrawer by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Style, 1: Camera, 2: Particles

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
    ) {
        // 1. Fullscreen 3D Falling Notes Engine & Touch Piano
        LyaneVisualizerView(
            sequence = sequence,
            currentTimeUs = positionUs,
            visualConfig = visualConfig,
            cameraConfig = cameraConfig,
            particleConfig = particleConfig,
            lightingConfig = lightingConfig,
            onNoteTriggered = { pitch, vel ->
                viewModel.midiManager.triggerVirtualNoteOn(pitch, vel)
            },
            onNoteReleased = { pitch ->
                viewModel.midiManager.triggerVirtualNoteOff(pitch)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = LyaneDarkSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sequence?.name ?: "Lyane Studio",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            // Quick Tools Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = LyaneDarkSurface.copy(alpha = 0.85f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
                ) {
                    IconButton(onClick = { showControlsDrawer = !showControlsDrawer }) {
                        Icon(Icons.Default.Tune, contentDescription = "Visual Controls", tint = LyaneCyan)
                    }
                }

                Surface(
                    color = LyaneDarkSurface.copy(alpha = 0.85f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
                ) {
                    IconButton(onClick = onNavigateToPresets) {
                        Icon(Icons.Default.Palette, contentDescription = "Presets", tint = LyanePurple)
                    }
                }

                Surface(
                    color = LyaneDarkSurface.copy(alpha = 0.85f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
                ) {
                    IconButton(onClick = onNavigateToEditor) {
                        Icon(Icons.Default.Edit, contentDescription = "Piano Roll", tint = LyaneAurora)
                    }
                }

                Surface(
                    color = LyaneCyan,
                    shape = CircleShape
                ) {
                    IconButton(onClick = onNavigateToExport) {
                        Icon(Icons.Default.Videocam, contentDescription = "Render Video", tint = LyaneBlack)
                    }
                }
            }
        }

        // 3. Floating Visual Settings Panel (when opened)
        AnimatedVisibility(
            visible = showControlsDrawer,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = LyaneDarkSurface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = LyaneCyan,
                        divider = {}
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Style") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("3D Camera") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Particles") })
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedTab) {
                        0 -> { // Style Selector
                            Text("Note Rendering Style:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(NoteStyle.NEON, NoteStyle.GLASS, NoteStyle.CRYSTAL, NoteStyle.ENERGY, NoteStyle.CLASSIC).forEach { style ->
                                    FilterChip(
                                        selected = visualConfig.noteStyle == style,
                                        onClick = { viewModel.setNoteStyle(style) },
                                        label = { Text(style.name) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LyaneCyan,
                                            selectedLabelColor = LyaneBlack
                                        )
                                    )
                                }
                            }
                        }
                        1 -> { // 3D Camera Controls
                            Text("Perspective Tilt Angle: ${cameraConfig.tiltAngle.toInt()}°", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Slider(
                                value = cameraConfig.tiltAngle,
                                onValueChange = { viewModel.setCameraTilt(it) },
                                valueRange = 0f..75f,
                                colors = SliderDefaults.colors(thumbColor = LyaneCyan, activeTrackColor = LyaneCyan)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dynamic Auto Camera Motion", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Switch(
                                    checked = cameraConfig.autoCamera,
                                    onCheckedChange = { viewModel.setAutoCamera(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = LyaneCyan)
                                )
                            }
                        }
                        2 -> { // Particle FX Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Note Reactive Particles", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Switch(
                                    checked = particleConfig.enabled,
                                    onCheckedChange = { viewModel.setParticlesEnabled(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = LyanePurple)
                                )
                            }
                            Text("Emission Amount: ${particleConfig.count}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Slider(
                                value = particleConfig.count.toFloat(),
                                onValueChange = { viewModel.setParticleCount(it.toInt()) },
                                valueRange = 20f..200f,
                                colors = SliderDefaults.colors(thumbColor = LyanePurple, activeTrackColor = LyanePurple)
                            )
                        }
                    }
                }
            }
        }

        // 4. Bottom Scrubber & Transport Timeline Bar
        LyaneTimelineBar(
            positionUs = positionUs,
            durationUs = sequence?.durationUs ?: 1L,
            isPlaying = isPlaying,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onStop = { viewModel.stop() },
            onSeekProgress = { viewModel.seekProgress(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 120.dp) // Above piano keys
        )
    }
}
