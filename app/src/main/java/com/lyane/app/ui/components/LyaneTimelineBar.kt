package com.lyane.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.theme.*

@Composable
fun LyaneTimelineBar(
    positionUs: Long,
    durationUs: Long,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeekProgress: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationUs > 0) (positionUs.toFloat() / durationUs).coerceIn(0f, 1f) else 0f

    val currentSec = positionUs / 1_000_000
    val totalSec = durationUs / 1_000_000
    val timeFormatted = String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LyaneDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Scrubber Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val frac = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeekProgress(frac)
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                // Background track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(LyaneCardSurface)
                )

                // Active progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(LyaneCyan, LyanePurple)))
                )

                // Thumb handle
                Box(
                    modifier = Modifier
                        .offset(x = (progress * 300).dp) // approximate visual offset
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(LyaneCyan)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Transport Controls & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LyaneCyan)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = LyaneBlack
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
