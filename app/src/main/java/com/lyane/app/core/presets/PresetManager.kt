package com.lyane.app.core.presets

import android.content.Context
import com.google.gson.Gson
import com.lyane.app.data.model.*
import java.io.File

data class PresetData(
    val id: String,
    val name: String,
    val description: String,
    val noteStyle: NoteStyle,
    val camera: CameraConfig,
    val particles: ParticleConfig,
    val lighting: LightingConfig,
    val colors: VisualConfig
)

class PresetManager(private val context: Context) {

    private val gson = Gson()
    private val presets = mutableListOf<PresetData>()
    private val customPresetsDir = File(context.filesDir, "custom_presets").apply { mkdirs() }

    init {
        loadBuiltInPresets()
        loadCustomPresets()
    }

    private fun loadBuiltInPresets() {
        val presetFiles = listOf(
            "moonlight.json", "lunar_glass.json", "aurora.json", "cyber_piano.json",
            "crystal.json", "cosmic.json", "minimal.json", "cinematic.json",
            "electric.json", "dream.json"
        )

        for (fileName in presetFiles) {
            try {
                context.assets.open("presets/$fileName").bufferedReader().use { reader ->
                    val preset = gson.fromJson(reader, PresetData::class.java)
                    if (preset != null) {
                        presets.add(preset)
                    }
                }
            } catch (e: Exception) {
                // Fallback default preset if asset loading fails
                if (fileName == "moonlight.json") {
                    presets.add(createDefaultMoonlightPreset())
                }
            }
        }
    }

    private fun loadCustomPresets() {
        customPresetsDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
            try {
                file.bufferedReader().use { reader ->
                    val preset = gson.fromJson(reader, PresetData::class.java)
                    if (preset != null) {
                        presets.add(preset)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllPresets(): List<PresetData> = presets

    fun getPresetById(id: String): PresetData? {
        return presets.find { it.id == id } ?: presets.firstOrNull()
    }

    fun saveCustomPreset(name: String, desc: String, visualConfig: VisualConfig, cameraConfig: CameraConfig, particleConfig: ParticleConfig, lightingConfig: LightingConfig): PresetData {
        val id = "custom_" + System.currentTimeMillis()
        val preset = PresetData(
            id = id,
            name = name,
            description = desc,
            noteStyle = visualConfig.noteStyle,
            camera = cameraConfig,
            particles = particleConfig,
            lighting = lightingConfig,
            colors = visualConfig
        )

        presets.add(preset)
        val file = File(customPresetsDir, "$id.json")
        file.writeText(gson.toJson(preset))
        return preset
    }

    fun exportPresetJson(preset: PresetData): String {
        return gson.toJson(preset)
    }

    fun importPresetJson(json: String): PresetData {
        val preset = gson.fromJson(json, PresetData::class.java)
        presets.add(preset)
        val file = File(customPresetsDir, "${preset.id}.json")
        file.writeText(json)
        return preset
    }

    private fun createDefaultMoonlightPreset(): PresetData {
        return PresetData(
            id = "preset_moonlight",
            name = "Moonlight",
            description = "Mysterious deep indigo night with glowing lunar cyan falling beams.",
            noteStyle = NoteStyle.NEON,
            camera = CameraConfig(tiltAngle = 48f, fov = 55f, zoom = 1.0f, panY = -0.15f, autoCamera = true),
            particles = ParticleConfig(enabled = true, count = 80, speed = 1.2f, lifetime = 1.5f, gravity = 0.05f, glow = 0.9f),
            lighting = LightingConfig(ambientColorHex = "#07080D", bloomIntensity = 1.4f, noteGlow = 1.5f, spotlightIntensity = 0.8f),
            colors = VisualConfig(noteStyle = NoteStyle.NEON, paletteType = ColorPaletteType.MOONLIGHT)
        )
    }
}
