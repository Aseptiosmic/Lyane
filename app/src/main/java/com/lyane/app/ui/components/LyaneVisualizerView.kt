package com.lyane.app.ui.components

import android.graphics.Canvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.lyane.app.core.visual.VisualEngine
import com.lyane.app.data.model.*
import kotlinx.coroutines.delay

@Composable
fun LyaneVisualizerView(
    sequence: MidiSequence?,
    currentTimeUs: Long,
    visualConfig: VisualConfig,
    cameraConfig: CameraConfig,
    particleConfig: ParticleConfig,
    lightingConfig: LightingConfig,
    onNoteTriggered: ((pitch: Int, velocity: Int) -> Unit)? = null,
    onNoteReleased: ((pitch: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visualEngine = remember { VisualEngine(context) }

    // 60 FPS frame ticker for particle animations and smooth falling
    var frameTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            frameTick = System.nanoTime()
            delay(16) // ~60 FPS
        }
    }

    var lastTouchedPitch by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(visualConfig) {
                    detectTapGestures(
                        onPress = { offset ->
                            val keyboardHeight = size.height * visualConfig.keyHeightRatio
                            val keyboardY = size.height - keyboardHeight
                            if (offset.y >= keyboardY) {
                                val pitch = visualEngine.keyboardRenderer.getPitchFromTouchX(offset.x, size.width.toFloat(), visualConfig)
                                lastTouchedPitch = pitch
                                onNoteTriggered?.invoke(pitch, 100)
                                val colorHex = visualConfig.chromaMap.getColorForPitchClass(pitch % 12)
                                visualEngine.keyboardRenderer.onNoteOn(pitch, 100, colorHex)
                                tryAwaitRelease()
                                onNoteReleased?.invoke(pitch)
                                visualEngine.keyboardRenderer.onNoteOff(pitch)
                                lastTouchedPitch = null
                            }
                        }
                    )
                }
        ) {
            // Read frameTick to trigger redraw
            val _t = frameTick
            val w = size.width
            val h = size.height

            drawIntoCanvas { canvas ->
                val nativeCanvas: Canvas = canvas.nativeCanvas
                visualEngine.renderFrame(
                    nativeCanvas,
                    w,
                    h,
                    sequence,
                    currentTimeUs,
                    visualConfig,
                    cameraConfig,
                    particleConfig,
                    lightingConfig
                )
            }
        }
    }
}
