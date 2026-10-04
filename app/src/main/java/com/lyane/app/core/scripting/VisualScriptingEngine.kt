package com.lyane.app.core.scripting

import com.lyane.app.data.model.*

enum class ScriptTrigger {
    ON_VELOCITY_GREATER_THAN,
    ON_PITCH_GREATER_THAN,
    ON_PITCH_LESS_THAN,
    ON_CHORD_DETECTED,
    ON_SUSTAIN_ACTIVE
}

enum class ScriptAction {
    BOOST_PARTICLES,
    TRIGGER_LIGHT_FLASH,
    TILT_CAMERA_DYNAMIC,
    CHANGE_NOTE_COLOR,
    BURST_COSMIC_SPECKLES
}

data class VisualRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var trigger: ScriptTrigger,
    var threshold: Int = 100,
    var action: ScriptAction,
    var actionIntensity: Float = 1.5f,
    var isEnabled: Boolean = true
)

class VisualScriptingEngine {

    val rules = mutableListOf<VisualRule>()

    init {
        // Default built-in smart visual rules
        rules.add(
            VisualRule(
                name = "Heavy Strike Explosion",
                trigger = ScriptTrigger.ON_VELOCITY_GREATER_THAN,
                threshold = 105,
                action = ScriptAction.BOOST_PARTICLES,
                actionIntensity = 2.0f
            )
        )
        rules.add(
            VisualRule(
                name = "High Register Shimmer",
                trigger = ScriptTrigger.ON_PITCH_GREATER_THAN,
                threshold = 84,
                action = ScriptAction.BURST_COSMIC_SPECKLES,
                actionIntensity = 1.6f
            )
        )
        rules.add(
            VisualRule(
                name = "Full Chord Dynamic Tilt",
                trigger = ScriptTrigger.ON_CHORD_DETECTED,
                threshold = 4,
                action = ScriptAction.TILT_CAMERA_DYNAMIC,
                actionIntensity = 1.3f
            )
        )
    }

    fun evaluateNoteEvent(
        note: MidiNote,
        activeChordCount: Int,
        particleConfig: ParticleConfig,
        cameraConfig: CameraConfig,
        lightingConfig: LightingConfig
    ) {
        for (rule in rules) {
            if (!rule.isEnabled) continue

            val triggered = when (rule.trigger) {
                ScriptTrigger.ON_VELOCITY_GREATER_THAN -> note.velocity >= rule.threshold
                ScriptTrigger.ON_PITCH_GREATER_THAN -> note.pitch >= rule.threshold
                ScriptTrigger.ON_PITCH_LESS_THAN -> note.pitch <= rule.threshold
                ScriptTrigger.ON_CHORD_DETECTED -> activeChordCount >= rule.threshold
                ScriptTrigger.ON_SUSTAIN_ACTIVE -> true
            }

            if (triggered) {
                applyAction(rule.action, rule.actionIntensity, particleConfig, cameraConfig, lightingConfig)
            }
        }
    }

    private fun applyAction(
        action: ScriptAction,
        intensity: Float,
        particleConfig: ParticleConfig,
        cameraConfig: CameraConfig,
        lightingConfig: LightingConfig
    ) {
        when (action) {
            ScriptAction.BOOST_PARTICLES -> {
                particleConfig.speed *= (1.0f + 0.1f * intensity)
            }
            ScriptAction.TRIGGER_LIGHT_FLASH -> {
                lightingConfig.bloomIntensity = (lightingConfig.bloomIntensity * intensity).coerceAtMost(3.0f)
            }
            ScriptAction.TILT_CAMERA_DYNAMIC -> {
                cameraConfig.tiltAngle = (cameraConfig.tiltAngle + 3.0f * intensity).coerceIn(20f, 75f)
            }
            ScriptAction.BURST_COSMIC_SPECKLES -> {
                particleConfig.glow = (particleConfig.glow * 1.2f).coerceAtMost(2.0f)
            }
            ScriptAction.CHANGE_NOTE_COLOR -> {}
        }
    }
}
