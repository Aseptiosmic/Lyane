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
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.components.LyanePresetCard
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun PresetsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val allPresets = viewModel.presetManager.getAllPresets()
    val currentProject by viewModel.currentProject.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Top Header
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
                    Text("Visual Preset Library", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text("Lunar & Neon visual atmospheres", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // AI Style Recommendations Section
        if (aiSuggestions.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LyaneDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LyanePurple)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LyanePurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Style Recommendation", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val top = aiSuggestions.first()
                        Text(
                            text = "${top.presetName} (${top.matchScore}% Match)",
                            style = MaterialTheme.typography.labelLarge,
                            color = LyaneCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = top.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.applyPreset(top.presetId) },
                            colors = ButtonDefaults.buttonColors(containerColor = LyanePurple, contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply ${top.presetName}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Preset List
        item {
            Text("Preset Collection (10 Built-in)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(allPresets) { preset ->
            LyanePresetCard(
                preset = preset,
                isSelected = (preset.id == currentProject.activePresetId),
                onSelect = { viewModel.applyPreset(preset.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
