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
import androidx.compose.ui.unit.dp
import com.lyane.app.ui.theme.*
import com.lyane.app.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LyaneBlack)
            .padding(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
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
                    Text("Lyane Settings & Privacy", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text("100% Free, Offline & Privacy-First", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Privacy Guarantee Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LyaneDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneAurora)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = LyaneAurora)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Privacy Guarantee", style = MaterialTheme.typography.titleMedium, color = LyaneAurora)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lyane operates completely on your local device. Your MIDI tracks, audio files, and exported videos are never uploaded to any remote server or cloud. No account registration, subscriptions, or watermarks required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Audio & Hardware Buffer
        item {
            Text("Audio Engine Configuration", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LyaneDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Synthesizer Sample Rate: 44.1 kHz 16-bit PCM Stereo", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text("Low-latency OpenSL / AudioTrack physical modeling active", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // About & Version
        item {
            Text("About Lyane", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LyaneDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LyaneBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lyane Studio v1.0.0", style = MaterialTheme.typography.titleMedium, color = LyaneCyan)
                    Text("Created for Pianists, Musicians & Visual Creators", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}
