package com.lyane.app.ui.screens

import android.content.Intent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.lyane.app.data.model.VideoAspectRatio
import com.lyane.app.data.model.VideoResolution
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun ExportScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val exportConfig by viewModel.exportConfig.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val exportResultFile by viewModel.exportResultFile.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
            .padding(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Export Cinematic Video", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text("High performance GPU accelerated MP4 render", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Aspect Ratio Selector
        item {
            Text("Aspect Ratio / Platform Target", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(VideoAspectRatio.RATIO_16_9, VideoAspectRatio.RATIO_9_16, VideoAspectRatio.RATIO_1_1).forEach { ratio ->
                    FilterChip(
                        selected = exportConfig.aspectRatio == ratio,
                        onClick = { viewModel.setExportAspectRatio(ratio) },
                        label = { Text(ratio.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LyaneCyan,
                            selectedLabelColor = LyaneBlack
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Resolution Selector
        item {
            Text("Output Resolution", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(VideoResolution.RES_720P, VideoResolution.RES_1080P, VideoResolution.RES_1440P, VideoResolution.RES_4K).forEach { res ->
                    FilterChip(
                        selected = exportConfig.resolution == res,
                        onClick = { viewModel.setExportResolution(res) },
                        label = { Text(res.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LyanePurple,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Framerate Selector
        item {
            Text("Framerate", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(24, 30, 60).forEach { fps ->
                    FilterChip(
                        selected = exportConfig.fps == fps,
                        onClick = { viewModel.setExportFps(fps) },
                        label = { Text("$fps FPS") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LyaneAurora,
                            selectedLabelColor = LyaneBlack
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Render Action & Progress Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LyaneDarkSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isExporting) {
                        Text("Rendering Video...", style = MaterialTheme.typography.titleMedium, color = LyaneCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = exportProgress / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = LyaneCyan,
                            trackColor = LyaneCardSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progress: $exportProgress%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            TextButton(onClick = { viewModel.cancelExport() }) {
                                Text("Cancel", color = Color(0xFFF87171))
                            }
                        }
                    } else if (exportResultFile != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LyaneAurora, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Export Complete!", style = MaterialTheme.typography.titleMedium, color = LyaneAurora)
                                Text(exportResultFile?.name ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val file = exportResultFile ?: return@Button
                                try {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Lyane Video"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LyaneAurora, contentColor = LyaneBlack),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Rendered Video")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startExport() },
                            colors = ButtonDefaults.buttonColors(containerColor = LyaneCyan, contentColor = LyaneBlack),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MovieCreation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Offline Video Render", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
