package com.lyane.app.data.model

import java.io.Serializable

data class CameraConfig(
    var tiltAngle: Float = 48.0f,       // Degrees tilt (0 = flat 2D top-down, 60 = steep 3D perspective)
    var fov: Float = 55.0f,             // Field of view in degrees
    var zoom: Float = 1.0f,             // 0.5x to 2.0x
    var panX: Float = 0.0f,             // Horizontal shift
    var panY: Float = -0.15f,           // Vertical shift
    var autoCamera: Boolean = true,     // Reactive camera motion with song dynamics
    var autoCameraIntensity: Float = 0.5f
) : Serializable

data class ParticleConfig(
    var enabled: Boolean = true,
    var count: Int = 80,                // Particle emission count per note hit
    var speed: Float = 1.2f,            // Initial burst velocity
    var lifetime: Float = 1.5f,         // Seconds
    var size: Float = 8.0f,             // Base particle radius in px
    var gravity: Float = 0.05f,         // Downward/upward acceleration
    var turbulence: Float = 0.3f,       // Chaotic air resistance
    var glow: Float = 0.9f,             // Particle emissive glow
    var spread: Float = 1.0f,           // Radial burst angle spread
    var opacity: Float = 0.95f
) : Serializable

data class LightingConfig(
    var ambientColorHex: String = "#07080D",
    var bloomIntensity: Float = 1.4f,
    var noteGlow: Float = 1.5f,
    var spotlightIntensity: Float = 0.8f,
    var velocityReactiveBrightness: Boolean = true,
    var directionalLightAngle: Float = 45.0f
) : Serializable
