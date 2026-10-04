package com.lyane.app.data.model

import java.io.Serializable

enum class NoteStyle {
    CLASSIC,
    NEON,
    GLASS,
    CRYSTAL,
    GEM,
    PARTICLE,
    ENERGY,
    WAVE,
    MINIMAL,
    COSMIC
}

enum class ColorPaletteType {
    AURORA,
    MOONLIGHT,
    NEON,
    OCEAN,
    SUNSET,
    GALAXY,
    MONOCHROME,
    CUSTOM
}

data class ChromaColorMap(
    var c: String = "#FF4B72",
    var cs: String = "#FF7E40",
    var d: String = "#FFAE19",
    var ds: String = "#F5E02A",
    var e: String = "#8CE033",
    var f: String = "#17E68C",
    var fs: String = "#00F0D0",
    var g: String = "#00D4FF",
    var gs: String = "#458DFF",
    var a: String = "#8562FF",
    var asharp: String = "#C44BFF",
    var b: String = "#FF3BB8"
) : Serializable {
    fun getColorForPitchClass(pc: Int): String {
        return when (pc % 12) {
            0 -> c
            1 -> cs
            2 -> d
            3 -> ds
            4 -> e
            5 -> f
            6 -> fs
            7 -> g
            8 -> gs
            9 -> a
            10 -> asharp
            11 -> b
            else -> c
        }
    }
}

data class VisualConfig(
    var noteStyle: NoteStyle = NoteStyle.NEON,
    var paletteType: ColorPaletteType = ColorPaletteType.MOONLIGHT,
    var chromaMap: ChromaColorMap = ChromaColorMap(),
    var keyCount: Int = 88,             // 88 standard, 76, 61
    var firstKeyPitch: Int = 21,        // 21 = A0
    var fallDurationMs: Long = 2500L,   // Visible falling note time horizon
    var noteRoundness: Float = 0.35f,
    var noteBorderGlow: Float = 0.85f,
    var showPianoKeys: Boolean = true,
    var keyHeightRatio: Float = 0.18f,
    var sustainPedalGlow: Boolean = true,
    var noteHitSplash: Boolean = true,
    var chordDetectionHighlight: Boolean = true,
    var backgroundDim: Float = 0.85f
) : Serializable
