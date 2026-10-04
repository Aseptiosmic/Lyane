package com.lyane.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.components.LyaneLogoView
import com.lyane.app.ui.theme.*

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val steps = listOf(
        OnboardingStep(
            title = "Welcome to Lyane",
            description = "A cinematic piano MIDI visualizer and video creator inspired by lunar polyphony.",
            icon = Icons.Default.MusicVideo,
            accentColor = LyaneCyan
        ),
        OnboardingStep(
            title = "Connect Live MIDI",
            description = "Plug in any USB OTG keyboard or digital piano for ultra low-latency real-time visualization.",
            icon = Icons.Default.Usb,
            accentColor = LyanePurple
        ),
        OnboardingStep(
            title = "Import MIDI & MusicXML",
            description = "Import .mid files or MusicXML scores and customize pitch colors, tempo, and notes.",
            icon = Icons.Default.FileOpen,
            accentColor = LyaneAurora
        ),
        OnboardingStep(
            title = "3D Shaders & Particles",
            description = "Experience 10 stunning visual styles with physics-based reactive particles and dynamic lighting.",
            icon = Icons.Default.AutoAwesome,
            accentColor = LyaneMagenta
        ),
        OnboardingStep(
            title = "Offline 4K Video Export",
            description = "Render YouTube 16:9, TikTok 9:16, or Square 1:1 videos directly on your device. 100% free, no watermarks.",
            icon = Icons.Default.Videocam,
            accentColor = LyaneAmber
        )
    )

    var currentStep by remember { mutableStateOf(0) }
    val step = steps[currentStep]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinish) {
                Text("Skip", color = TextSecondary)
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (currentStep == 0) {
                LyaneLogoView(size = 96.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(step.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(step.icon, contentDescription = null, tint = step.accentColor, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Bottom Controls
        Column(modifier = Modifier.fillMaxWidth()) {
            // Step Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                steps.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == currentStep) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (i == currentStep) LyaneCyan else LyaneCardSurface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        onFinish()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LyaneCyan, contentColor = LyaneBlack),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (currentStep < steps.size - 1) "Continue" else "Get Started",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
