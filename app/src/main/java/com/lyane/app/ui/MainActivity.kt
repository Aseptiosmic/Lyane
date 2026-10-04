package com.lyane.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyane.app.ui.screens.*
import com.lyane.app.ui.theme.LyaneTheme
import com.lyane.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle intent if launched via file open
        handleIntent(intent)

        setContent {
            LyaneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Activity Result Launchers for File Import
                    val midiPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            viewModel.importMidiFromUri(it)
                            navController.navigate("studio")
                        }
                    }

                    val xmlPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            viewModel.importMusicXmlFromUri(it)
                            navController.navigate("studio")
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinish = {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToStudio = { navController.navigate("studio") },
                                onNavigateToEditor = { navController.navigate("editor") },
                                onNavigateToPresets = { navController.navigate("presets") },
                                onNavigateToExport = { navController.navigate("export") },
                                onOpenMidiPicker = { midiPickerLauncher.launch("audio/*") },
                                onOpenXmlPicker = { xmlPickerLauncher.launch("text/xml") }
                            )
                        }

                        composable("studio") {
                            StudioScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEditor = { navController.navigate("editor") },
                                onNavigateToExport = { navController.navigate("export") },
                                onNavigateToPresets = { navController.navigate("presets") }
                            )
                        }

                        composable("editor") {
                            EditorScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("presets") {
                            PresetsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("timeline") {
                            TimelineScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("export") {
                            ExportScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            val scheme = data.scheme
            val type = intent.type
            if (type?.contains("midi") == true || data.toString().endsWith(".mid") || data.toString().endsWith(".midi")) {
                viewModel.importMidiFromUri(data)
            } else if (type?.contains("xml") == true || data.toString().endsWith(".musicxml") || data.toString().endsWith(".xml")) {
                viewModel.importMusicXmlFromUri(data)
            }
        }
    }
}
