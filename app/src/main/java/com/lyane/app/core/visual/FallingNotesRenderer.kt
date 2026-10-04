package com.lyane.app.core.visual

import android.graphics.*
import com.lyane.app.core.camera.Camera3DController
import com.lyane.app.data.model.*
import kotlin.math.*

class FallingNotesRenderer {

    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val hitSplashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun draw(
        canvas: Canvas,
        sequence: MidiSequence?,
        currentTimeUs: Long,
        width: Float,
        height: Float,
        keyboardY: Float,
        visualConfig: VisualConfig,
        cameraController: Camera3DController,
        keyboardRenderer: KeyboardRenderer
    ) {
        val horizonUs = visualConfig.fallDurationMs * 1000L
        val visibleStartUs = currentTimeUs
        val visibleEndUs = currentTimeUs + horizonUs

        if (sequence == null) return

        val visibleNotes = sequence.getNotesInRange(visibleStartUs - 500_000L, visibleEndUs)

        val firstPitch = visualConfig.firstKeyPitch
        val numKeys = visualConfig.keyCount
        val pitchStepNorm = 2.0f / numKeys.toFloat()

        for (note in visibleNotes) {
            val pitch = note.pitch
            if (pitch < firstPitch || pitch >= firstPitch + numKeys) continue

            val pitchNormX = keyboardRenderer.getPitchNormX(pitch, visualConfig)
            val noteWidthNorm = pitchStepNorm * 0.85f

            // Start & End Y in falling space (0 = hitting keyboard line, 1 = top horizon)
            val noteStartNormY = ((note.startTimeUs - currentTimeUs).toFloat() / horizonUs).coerceIn(-0.2f, 1.2f)
            val noteEndNormY = ((note.endTimeUs - currentTimeUs).toFloat() / horizonUs).coerceIn(-0.2f, 1.2f)

            // Skip notes that have completely passed below keyboard
            if (noteEndNormY < -0.05f) continue

            // Bottom point (where note begins / hits keyboard)
            val clampedBottomY = maxOf(0.0f, noteStartNormY)
            val clampedTopY = maxOf(0.02f, noteEndNormY)

            val (bottomLeftX, bottomLeftY) = cameraController.projectPoint(
                pitchNormX - noteWidthNorm / 2.0f, clampedBottomY, width, height, keyboardY
            )
            val (bottomRightX, bottomRightY) = cameraController.projectPoint(
                pitchNormX + noteWidthNorm / 2.0f, clampedBottomY, width, height, keyboardY
            )
            val (topLeftX, topLeftY) = cameraController.projectPoint(
                pitchNormX - noteWidthNorm / 2.0f, clampedTopY, width, height, keyboardY
            )
            val (topRightX, topRightY) = cameraController.projectPoint(
                pitchNormX + noteWidthNorm / 2.0f, clampedTopY, width, height, keyboardY
            )

            // Base color from Chroma 12-pitch map
            val colorHex = note.colorHex ?: visualConfig.chromaMap.getColorForPitchClass(note.pitchClass)
            val parsedColor = try { Color.parseColor(colorHex) } catch (_: Exception) { Color.CYAN }

            val velocityFactor = (note.velocity / 127.0f).coerceIn(0.4f, 1.0f)
            val baseAlpha = (255 * velocityFactor).toInt().coerceIn(40, 255)

            // Construct 3D quad path
            val notePath = Path().apply {
                moveTo(bottomLeftX, bottomLeftY)
                lineTo(bottomRightX, bottomRightY)
                lineTo(topRightX, topRightY)
                lineTo(topLeftX, topLeftY)
                close()
            }

            // Render according to selected NoteStyle
            when (visualConfig.noteStyle) {
                NoteStyle.CLASSIC -> {
                    notePaint.color = Color.argb(baseAlpha, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    canvas.drawPath(notePath, notePaint)
                }

                NoteStyle.NEON -> {
                    // Outer neon glow
                    glowPaint.color = Color.argb((baseAlpha * 0.45f).toInt(), Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    borderPaint.strokeWidth = 6.0f * cameraController.getZoom()
                    borderPaint.color = glowPaint.color
                    canvas.drawPath(notePath, borderPaint)

                    // Core bright note
                    notePaint.color = Color.argb(baseAlpha, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    canvas.drawPath(notePath, notePaint)

                    // Glowing center spine
                    borderPaint.strokeWidth = 2.0f
                    borderPaint.color = Color.argb((baseAlpha * 0.8f).toInt(), 255, 255, 255)
                    val (midBotX, midBotY) = cameraController.projectPoint(pitchNormX, clampedBottomY, width, height, keyboardY)
                    val (midTopX, midTopY) = cameraController.projectPoint(pitchNormX, clampedTopY, width, height, keyboardY)
                    canvas.drawLine(midBotX, midBotY, midTopX, midTopY, borderPaint)
                }

                NoteStyle.GLASS -> {
                    notePaint.color = Color.argb((baseAlpha * 0.5f).toInt(), Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    canvas.drawPath(notePath, notePaint)

                    // Frosted edge highlight
                    borderPaint.strokeWidth = 2.5f
                    borderPaint.color = Color.argb(200, 240, 245, 255)
                    canvas.drawPath(notePath, borderPaint)
                }

                NoteStyle.CRYSTAL -> {
                    notePaint.color = Color.argb(baseAlpha, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    canvas.drawPath(notePath, notePaint)

                    // Facet diagonals
                    borderPaint.strokeWidth = 1.5f
                    borderPaint.color = Color.argb(160, 255, 255, 255)
                    canvas.drawLine(bottomLeftX, bottomLeftY, topRightX, topRightY, borderPaint)
                    canvas.drawLine(bottomRightX, bottomRightY, topLeftX, topLeftY, borderPaint)
                }

                NoteStyle.ENERGY -> {
                    // Electric plasma pulse
                    val pulse = (sin(System.currentTimeMillis() * 0.01 + pitch) * 0.2 + 0.8).toFloat()
                    notePaint.color = Color.argb((baseAlpha * pulse).toInt().coerceIn(50, 255), Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    canvas.drawPath(notePath, notePaint)

                    borderPaint.strokeWidth = 4.0f
                    borderPaint.color = Color.argb(230, 255, 255, 255)
                    canvas.drawPath(notePath, borderPaint)
                }

                NoteStyle.GEM, NoteStyle.COSMIC, NoteStyle.PARTICLE, NoteStyle.WAVE, NoteStyle.MINIMAL -> {
                    notePaint.color = Color.argb(baseAlpha, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                    canvas.drawPath(notePath, notePaint)

                    borderPaint.strokeWidth = 1.5f
                    borderPaint.color = Color.argb((baseAlpha * 0.7f).toInt(), 255, 255, 255)
                    canvas.drawPath(notePath, borderPaint)
                }
            }

            // Active hit contact splash at keyboard line
            if (currentTimeUs in note.startTimeUs..note.endTimeUs && visualConfig.noteHitSplash) {
                val hitAlpha = ((1.0f - (currentTimeUs - note.startTimeUs).toFloat() / 300_000f).coerceIn(0.2f, 1.0f) * 220).toInt()
                hitSplashPaint.color = Color.argb(hitAlpha, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                val (hitCenterX, hitCenterY) = cameraController.projectPoint(pitchNormX, 0.0f, width, height, keyboardY)
                val splashRadius = 14.0f * (note.velocity / 100.0f) * cameraController.getZoom()
                canvas.drawCircle(hitCenterX, hitCenterY, splashRadius, hitSplashPaint)
            }
        }
    }
}
