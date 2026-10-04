package com.lyane.app.core.camera

import android.graphics.Matrix
import com.lyane.app.data.model.CameraConfig
import kotlin.math.*

class Camera3DController {

    private var currentTilt = 48.0f
    private var currentZoom = 1.0f
    private var currentPanX = 0.0f
    private var currentPanY = -0.15f

    private var smoothedEnergy = 0.0f

    fun updateAutoCamera(recentVelocitySum: Float, config: CameraConfig, deltaTimeSec: Float) {
        if (!config.autoCamera) {
            currentTilt = config.tiltAngle
            currentZoom = config.zoom
            currentPanX = config.panX
            currentPanY = config.panY
            return
        }

        // Smooth energy tracking
        val targetEnergy = (recentVelocitySum / 400.0f).coerceIn(0.0f, 1.0f)
        smoothedEnergy += (targetEnergy - smoothedEnergy) * (deltaTimeSec * 4.0f)

        val intensity = config.autoCameraIntensity
        val targetTilt = config.tiltAngle + (smoothedEnergy * 10.0f * intensity)
        val targetZoom = config.zoom + (smoothedEnergy * 0.12f * intensity)
        val targetPanY = config.panY + (smoothedEnergy * 0.05f * intensity)

        currentTilt += (targetTilt - currentTilt) * (deltaTimeSec * 5.0f)
        currentZoom += (targetZoom - currentZoom) * (deltaTimeSec * 5.0f)
        currentPanX = config.panX
        currentPanY += (targetPanY - currentPanY) * (deltaTimeSec * 5.0f)
    }

    /**
     * Projects 2D normalized (x: -1..1, y: 0..1 [0 is keyboard line, 1 is top horizon])
     * into 3D perspective screen coordinates.
     */
    fun projectPoint(
        normX: Float,
        normY: Float,
        screenWidth: Float,
        screenHeight: Float,
        keyboardY: Float
    ): Pair<Float, Float> {
        val radTilt = Math.toRadians(currentTilt.toDouble()).toFloat()
        
        // Depth scale based on perspective tilt
        // At keyboardY (normY = 0), perspective factor is 1.0
        // At top horizon (normY = 1), perspective factor narrows by cos(tilt)
        val perspectiveNarrow = 1.0f - (normY * sin(radTilt) * 0.55f)
        val projectedNormX = normX * perspectiveNarrow * currentZoom

        val screenCenterX = screenWidth / 2.0f + (currentPanX * screenWidth)
        val screenX = screenCenterX + (projectedNormX * (screenWidth * 0.48f))

        val availableHeight = keyboardY
        val nonLinearY = (1.0f - (1.0f - normY).pow(1.0f + sin(radTilt) * 0.6f))
        val screenY = keyboardY - (nonLinearY * availableHeight * cos(radTilt) * currentZoom)

        return Pair(screenX, screenY + (currentPanY * screenHeight))
    }

    fun getTiltAngle(): Float = currentTilt
    fun getZoom(): Float = currentZoom
}
