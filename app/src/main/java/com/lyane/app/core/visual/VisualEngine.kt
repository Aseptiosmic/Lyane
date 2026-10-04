package com.lyane.app.core.visual

import android.content.Context
import android.graphics.*
import com.lyane.app.core.camera.Camera3DController
import com.lyane.app.core.particles.ParticleEngine
import com.lyane.app.data.model.*

class VisualEngine(private val context: Context) {

    val keyboardRenderer = KeyboardRenderer()
    val fallingNotesRenderer = FallingNotesRenderer()
    val particleEngine = ParticleEngine()
    val cameraController = Camera3DController()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.0f
        color = Color.parseColor("#152238")
    }

    private var lastFrameTimeNanos = 0L

    fun onNoteOn(note: MidiNote, visualConfig: VisualConfig, particleConfig: ParticleConfig, width: Float, height: Float, keyboardY: Float) {
        val colorHex = note.colorHex ?: visualConfig.chromaMap.getColorForPitchClass(note.pitchClass)
        keyboardRenderer.onNoteOn(note.pitch, note.velocity, colorHex)

        val pitchNormX = keyboardRenderer.getPitchNormX(note.pitch, visualConfig)
        val (hitX, hitY) = cameraController.projectPoint(pitchNormX, 0.0f, width, height, keyboardY)

        particleEngine.emitNoteHit(hitX, hitY, colorHex, note.velocity, particleConfig)
    }

    fun onNoteOff(pitch: Int) {
        keyboardRenderer.onNoteOff(pitch)
    }

    fun renderFrame(
        canvas: Canvas,
        width: Float,
        height: Float,
        sequence: MidiSequence?,
        currentTimeUs: Long,
        visualConfig: VisualConfig,
        cameraConfig: CameraConfig,
        particleConfig: ParticleConfig,
        lightingConfig: LightingConfig
    ) {
        val now = System.nanoTime()
        val deltaSec = if (lastFrameTimeNanos == 0L) 0.016f else ((now - lastFrameTimeNanos) / 1_000_000_000.0f).coerceIn(0.001f, 0.1f)
        lastFrameTimeNanos = now

        val keyboardHeight = height * visualConfig.keyHeightRatio
        val keyboardY = height - keyboardHeight

        // Calculate dynamic velocity energy sum
        val activeNotes = sequence?.getActiveNotesAt(currentTimeUs) ?: emptyList()
        val velocitySum = activeNotes.sumOf { it.velocity }.toFloat()

        // 1. Update Camera motion
        cameraController.updateAutoCamera(velocitySum, cameraConfig, deltaSec)

        // 2. Draw Lunar Space Background
        val bgColor = try { Color.parseColor(lightingConfig.ambientColorHex) } catch (_: Exception) { Color.parseColor("#07080D") }
        bgPaint.color = bgColor
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 3. Draw Perspective 3D Grid floor lines
        drawPerspectiveGrid(canvas, width, height, keyboardY, visualConfig)

        // 4. Draw Falling Notes
        fallingNotesRenderer.draw(
            canvas,
            sequence,
            currentTimeUs,
            width,
            height,
            keyboardY,
            visualConfig,
            cameraController,
            keyboardRenderer
        )

        // 5. Update and Draw Particles
        particleEngine.update(deltaSec, particleConfig)
        particleEngine.draw(canvas)

        // 6. Draw Piano Keyboard
        keyboardRenderer.draw(canvas, width, height, keyboardY, visualConfig)
    }

    private fun drawPerspectiveGrid(canvas: Canvas, width: Float, height: Float, keyboardY: Float, config: VisualConfig) {
        val numKeys = config.keyCount
        val step = 4 // Draw grid line every 4 keys (octaves)

        for (p in config.firstKeyPitch until (config.firstKeyPitch + numKeys) step step) {
            val normX = keyboardRenderer.getPitchNormX(p, config)
            val (botX, botY) = cameraController.projectPoint(normX, 0.0f, width, height, keyboardY)
            val (topX, topY) = cameraController.projectPoint(normX, 1.0f, width, height, keyboardY)
            canvas.drawLine(botX, botY, topX, topY, gridPaint)
        }

        // Horizontal perspective depth rings
        for (i in 1..4) {
            val normY = i * 0.25f
            val (leftX, leftY) = cameraController.projectPoint(-1.0f, normY, width, height, keyboardY)
            val (rightX, rightY) = cameraController.projectPoint(1.0f, normY, width, height, keyboardY)
            canvas.drawLine(leftX, leftY, rightX, rightY, gridPaint)
        }
    }

    fun clear() {
        keyboardRenderer.clear()
        particleEngine.clear()
        lastFrameTimeNanos = 0L
    }
}
