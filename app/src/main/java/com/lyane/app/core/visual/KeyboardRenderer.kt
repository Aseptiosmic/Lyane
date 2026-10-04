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

    private data class KeyLayout(val firstPitch: Int, val numKeys: Int, val whitePitches: List<Int>)

    /** Builds the same white-key list/order that [draw] uses, so all geometry stays consistent. */
    private fun buildKeyLayout(config: VisualConfig): KeyLayout {
        val firstPitch = config.firstKeyPitch
        val numKeys = config.keyCount
        val whitePitches = mutableListOf<Int>()
        for (p in firstPitch until (firstPitch + numKeys)) {
            val pc = p % 12
            if (pc != 1 && pc != 3 && pc != 6 && pc != 8 && pc != 10) {
                whitePitches.add(p)
            }
        }
        return KeyLayout(firstPitch, numKeys, whitePitches)
    }

    /** Horizontal center of [pitch], in white-key-width units, matching [draw]'s layout exactly. */
    private fun whiteUnitXForPitch(pitch: Int, layout: KeyLayout): Float? {
        val pc = ((pitch % 12) + 12) % 12
        val isBlack = pc == 1 || pc == 3 || pc == 6 || pc == 8 || pc == 10
        return if (!isBlack) {
            val i = layout.whitePitches.indexOf(pitch)
            if (i < 0) null else i + 0.5f
        } else {
            // In draw(), a black key is centered on the boundary right after its preceding white key.
            val precedingWhite = pitch - 1
            val i = layout.whitePitches.indexOf(precedingWhite)
            if (i < 0) null else i + 1f
        }
    }

    /**
     * Maps pitch to normalized X coordinate (-1.0 to +1.0), using real white/black key
     * geometry (matching [draw]) instead of naive chromatic spacing — this keeps falling
     * notes aligned with the keys actually drawn on screen.
     */
    fun getPitchNormX(pitch: Int, config: VisualConfig): Float {
        val layout = buildKeyLayout(config)
        if (layout.whitePitches.isEmpty()) return 0f
        val whiteUnitX = whiteUnitXForPitch(pitch, layout) ?: (layout.whitePitches.size / 2f)
        val norm = (whiteUnitX / layout.whitePitches.size.toFloat()).coerceIn(0f, 1f)
        return (norm * 2.0f - 1.0f).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Finds the pitch under a screen touch X coordinate. Checks black keys first (they are
     * drawn on top of white keys with a narrower hit box centered on the white-key boundary,
     * exactly as [draw] renders them) before falling back to the underlying white key.
     */
    fun getPitchFromTouchX(touchX: Float, width: Float, config: VisualConfig): Int {
        val layout = buildKeyLayout(config)
        val numWhite = layout.whitePitches.size
        if (numWhite == 0) return config.firstKeyPitch
        val whiteKeyWidth = width / numWhite.toFloat()
        val blackKeyWidth = whiteKeyWidth * 0.62f

        for (i in layout.whitePitches.indices) {
            val pitch = layout.whitePitches[i]
            val pc = pitch % 12
            if (pc == 0 || pc == 2 || pc == 5 || pc == 7 || pc == 9) {
                val blackPitch = pitch + 1
                if (blackPitch < layout.firstPitch + layout.numKeys) {
                    val centerX = (i + 1) * whiteKeyWidth
                    val left = centerX - blackKeyWidth / 2f
                    val right = centerX + blackKeyWidth / 2f
                    if (touchX in left..right) return blackPitch
                }
            }
        }

        val whiteIndex = (touchX / whiteKeyWidth).toInt().coerceIn(0, numWhite - 1)
        return layout.whitePitches[whiteIndex]
    }
}
