package com.lyane.app.core.visual

import android.graphics.*
import com.lyane.app.data.model.VisualConfig

class KeyboardRenderer {

    private val whiteKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#F1F5F9")
    }

    private val blackKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#0F111A")
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        color = Color.parseColor("#334155")
    }

    private val activeKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val keyGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Active key presses (pitch -> (velocity, colorHex, timestamp))
    private val activeKeys = mutableMapOf<Int, ActiveKeyInfo>()

    data class ActiveKeyInfo(val pitch: Int, val velocity: Int, val colorHex: String)

    fun onNoteOn(pitch: Int, velocity: Int, colorHex: String) {
        synchronized(activeKeys) {
            activeKeys[pitch] = ActiveKeyInfo(pitch, velocity, colorHex)
        }
    }

    fun onNoteOff(pitch: Int) {
        synchronized(activeKeys) {
            activeKeys.remove(pitch)
        }
    }

    fun clear() {
        synchronized(activeKeys) {
            activeKeys.clear()
        }
    }

    fun draw(canvas: Canvas, width: Float, height: Float, keyboardY: Float, config: VisualConfig) {
        if (!config.showPianoKeys) return

        val keyHeight = height - keyboardY
        val firstPitch = config.firstKeyPitch // 21 (A0)
        val numKeys = config.keyCount // 88

        // Count white keys
        val whitePitches = mutableListOf<Int>()
        for (p in firstPitch until (firstPitch + numKeys)) {
            val pc = p % 12
            if (pc != 1 && pc != 3 && pc != 6 && pc != 8 && pc != 10) {
                whitePitches.add(p)
            }
        }
        val whiteKeyWidth = width / whitePitches.size.toFloat()

        // 1. Draw White Keys
        for (i in whitePitches.indices) {
            val pitch = whitePitches[i]
            val left = i * whiteKeyWidth
            val right = left + whiteKeyWidth
            val top = keyboardY
            val bottom = height

            val active = synchronized(activeKeys) { activeKeys[pitch] }
            if (active != null) {
                try {
                    val baseColor = Color.parseColor(active.colorHex)
                    activeKeyPaint.color = baseColor
                    canvas.drawRect(left, top, right, bottom, activeKeyPaint)

                    // Key splash glow up into falling space
                    keyGlowPaint.shader = LinearGradient(
                        left, top, left, top - 80f,
                        Color.argb((active.velocity * 1.8f).toInt().coerceIn(50, 220), Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(left, top - 80f, right, top, keyGlowPaint)
                } catch (_: Exception) {
                    canvas.drawRect(left, top, right, bottom, whiteKeyPaint)
                }
            } else {
                canvas.drawRect(left, top, right, bottom, whiteKeyPaint)
            }
            canvas.drawRect(left, top, right, bottom, borderPaint)
        }

        // 2. Draw Black Keys (overlay)
        val blackKeyWidth = whiteKeyWidth * 0.62f
        val blackKeyHeight = keyHeight * 0.65f

        for (i in whitePitches.indices) {
            val pitch = whitePitches[i]
            val pc = pitch % 12
            // If there's a black key above this white key (C, D, F, G, A)
            if (pc == 0 || pc == 2 || pc == 5 || pc == 7 || pc == 9) {
                val blackPitch = pitch + 1
                if (blackPitch < firstPitch + numKeys) {
                    val centerX = (i + 1) * whiteKeyWidth
                    val left = centerX - blackKeyWidth / 2.0f
                    val right = centerX + blackKeyWidth / 2.0f
                    val top = keyboardY
                    val bottom = top + blackKeyHeight

                    val active = synchronized(activeKeys) { activeKeys[blackPitch] }
                    if (active != null) {
                        try {
                            val baseColor = Color.parseColor(active.colorHex)
                            activeKeyPaint.color = baseColor
                            canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, activeKeyPaint)

                            keyGlowPaint.shader = LinearGradient(
                                left, top, left, top - 90f,
                                Color.argb((active.velocity * 2.0f).toInt().coerceIn(60, 240), Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)),
                                Color.TRANSPARENT,
                                Shader.TileMode.CLAMP
                            )
                            canvas.drawRect(left, top - 90f, right, top, keyGlowPaint)
                        } catch (_: Exception) {
                            canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, blackKeyPaint)
                        }
                    } else {
                        canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, blackKeyPaint)
                    }
                }
            }
        }
    }

    /**
     * Maps pitch to normalized X coordinate (-1.0 to +1.0)
     */
    fun getPitchNormX(pitch: Int, config: VisualConfig): Float {
        val firstPitch = config.firstKeyPitch
        val numKeys = config.keyCount
        val norm = (pitch - firstPitch).toFloat() / (numKeys - 1).toFloat()
        return (norm * 2.0f - 1.0f).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Finds pitch from screen touch X coordinate
     */
    fun getPitchFromTouchX(touchX: Float, width: Float, config: VisualConfig): Int {
        val norm = (touchX / width).coerceIn(0.0f, 1.0f)
        val pitch = config.firstKeyPitch + (norm * (config.keyCount - 1)).toInt()
        return pitch.coerceIn(config.firstKeyPitch, config.firstKeyPitch + config.keyCount - 1)
    }
}
