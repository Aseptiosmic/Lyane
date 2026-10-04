package com.lyane.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.components.LyaneLogoView
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToExport: () -> Unit,
    onOpenMidiPicker: () -> Unit,
    onOpenXmlPicker: () -> Unit
) {
    val currentSequence by viewModel.currentSequence.collectAsState()
    val isDeviceConnected by viewModel.midiManager.isDeviceConnected.collectAsState()
    val connectedDevices by viewModel.midiManager.connectedDevices.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        // 1. Lyane Hero Brand Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                color = LyaneDarkSurface,
                border = BorderStroke(1.dp, LyaneBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(LyaneIndigo.copy(alpha = 0.25f), Color.Transparent),
                                radius = 600f
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "LYANE",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Piano MIDI Visualizer & Video Studio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LyaneCyan
                                )
                            }
                            LyaneLogoView(size = 56.dp)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Next-generation 3D falling notes, reactive particles & offline 4K video rendering. 100% free & offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick launch studio button
                        Button(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LyaneCyan,
                                contentColor = LyaneBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentSequence != null) "Open Studio (${currentSequence?.name})" else "Launch Studio",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // 2. Hardware / Live MIDI Status Banner
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isDeviceConnected) Color(0xFF06281D) else LyaneDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDeviceConnected) LyaneAurora else LyaneBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDeviceConnected) LyaneAurora else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDeviceConnected) "MIDI Device Connected" else "Live MIDI Input Ready",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isDeviceConnected) LyaneAurora else TextSecondary
                        )
                        Text(
                            text = if (isDeviceConnected) connectedDevices.joinToString() else "Connect USB OTG keyboard or digital piano",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    TextButton(onClick = { viewModel.midiManager.refreshDevices() }) {
                        Text("Scan", color = LyaneCyan)
                    }
                }
            }
        }

        // 3. Quick Action Grid
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "Import MIDI",
                    subtitle = ".mid / .midi file",
                    icon = Icons.Default.FileOpen,
                    accentColor = LyaneCyan,
                    onClick = onOpenMidiPicker,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "MusicXML",
                    subtitle = "Score import",
                    icon = Icons.Default.Description,
                    accentColor = LyanePurple,
                    onClick = onOpenXmlPicker,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "MIDI Editor",
                    subtitle = "Piano roll & quantize",
                    icon = Icons.Default.Edit,
                    accentColor = LyaneAurora,
                    onClick = onNavigateToEditor,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Export Video",
                    subtitle = "4K 60FPS MP4",
                    icon = Icons.Default.MovieCreation,
                    accentColor = LyaneMagenta,
                    onClick = onNavigateToExport,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Built-in Masterpiece Demos
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Demo Masterpieces",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            DemoSongRow(
                title = "Moonlight Sonata (Op. 27 No. 2)",
                composer = "Ludwig van Beethoven",
                genre = "Classical / Nocturne",
                onClick = {
                    viewModel.loadDemoSong("moonlight_sonata")
                    viewModel.applyPreset("preset_moonlight")
                    onNavigateToStudio()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DemoSongRow(
                title = "Clair de Lune (Suite Bergamasque)",
                composer = "Claude Debussy",
                genre = "Impressionist / Ethereal",
                onClick = {
                    viewModel.loadDemoSong("clair_de_lune")
                    viewModel.applyPreset("preset_lunar_glass")
                    onNavigateToStudio()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DemoSongRow(
                title = "Prelude in C Major (BWV 846)",
                composer = "Johann Sebastian Bach",
                genre = "Baroque / Polyphonic",
                onClick = {
                    viewModel.loadDemoSong("bach_prelude")
                    viewModel.applyPreset("preset_aurora")
                    onNavigateToStudio()
                }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = LyaneDarkSurface,
        border = BorderStroke(1.dp, LyaneBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
fun DemoSongRow(
    title: String,
    composer: String,
    genre: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = LyaneDarkSurface,
        border = BorderStroke(1.dp, LyaneBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LyaneCardSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = LyaneCyan)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = "$composer • $genre", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
