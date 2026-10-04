package com.lyane.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyane.app.core.presets.PresetData
import com.lyane.app.ui.theme.*

@Composable
fun LyanePresetCard(
    preset: PresetData,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        color = if (isSelected) LyaneCardSurface else LyaneDarkSurface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) LyaneCyan else LyaneBorder
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Style Emblem Preview
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            when (preset.id) {
                                "preset_moonlight" -> listOf(LyaneCyan, LyaneIndigo)
                                "preset_aurora" -> listOf(LyaneAurora, LyaneCyan)
                                "preset_cyber_piano" -> listOf(LyaneMagenta, LyaneCyan)
                                "preset_cinematic" -> listOf(LyaneAmber, LyaneMagenta)
                                else -> listOf(LyanePurple, LyaneCyanGlow)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = LyaneBlack
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) LyaneCyan else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = LyaneBorder,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = preset.noteStyle.name,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 2
                )
            }
        }
    }
}
