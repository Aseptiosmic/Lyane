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

    companion object {
        /**
         * Returns a distinct 12-pitch-class color map for each visual theme.
         * CUSTOM passes through [fallback] (the user's own/current map) unchanged.
         */
        fun forPalette(type: ColorPaletteType, fallback: ChromaColorMap = ChromaColorMap()): ChromaColorMap {
            return when (type) {
                ColorPaletteType.AURORA -> ChromaColorMap(
                    c = "#00F5A0", cs = "#00E0B8", d = "#00C9D0", ds = "#00AEE8",
                    e = "#2E96FF", f = "#6FDC8C", fs = "#36D399", g = "#17E6B4",
                    gs = "#00D6C8", a = "#00BBE0", asharp = "#3AA0FF", b = "#7CFFCB"
                )
                ColorPaletteType.MOONLIGHT -> ChromaColorMap(
                    c = "#8FD3FF", cs = "#7CC2FF", d = "#6AB0FF", ds = "#5A9EFF",
                    e = "#4D8CFA", f = "#9FE3FF", fs = "#89D6FF", g = "#73C8FF",
                    gs = "#B9ECFF", a = "#A3DFFF", asharp = "#5E7FE0", b = "#C9D8FF"
                )
                ColorPaletteType.NEON -> ChromaColorMap(
                    c = "#FF00E5", cs = "#FF2BD6", d = "#FF00A8", ds = "#F500FF",
                    e = "#B400FF", f = "#00FFF0", fs = "#00E5FF", g = "#00B8FF",
                    gs = "#2BFFEF", a = "#FFEA00", asharp = "#FF9900", b = "#FF2E63"
                )
                ColorPaletteType.OCEAN -> ChromaColorMap(
                    c = "#00B4D8", cs = "#0096C7", d = "#0077B6", ds = "#023E8A",
                    e = "#48CAE4", f = "#90E0EF", fs = "#ADE8F4", g = "#00F5D4",
                    gs = "#00C2A8", a = "#007F7A", asharp = "#4EA8DE", b = "#CAF0F8"
                )
                ColorPaletteType.SUNSET -> ChromaColorMap(
                    c = "#FF6B35", cs = "#FF8C42", d = "#FFA552", ds = "#FFC93C",
                    e = "#FFD23F", f = "#FF5D73", fs = "#F72C5B", g = "#C81D6C",
                    gs = "#FF9E7D", a = "#FFB38A", asharp = "#E8590C", b = "#FF4D4D"
                )
                ColorPaletteType.GALAXY -> ChromaColorMap(
                    c = "#B983FF", cs = "#9D65FF", d = "#7B4DFF", ds = "#5A3FE0",
                    e = "#3F2CB3", f = "#D291FF", fs = "#E6A8FF", g = "#8862F0",
                    gs = "#5E3BE8", a = "#C06CFF", asharp = "#4527A0", b = "#F0C3FF"
                )
                ColorPaletteType.MONOCHROME -> ChromaColorMap(
                    c = "#FFFFFF", cs = "#ECECEC", d = "#D9D9D9", ds = "#C6C6C6",
                    e = "#B3B3B3", f = "#F5F5F5", fs = "#E0E0E0", g = "#CCCCCC",
                    gs = "#A6A6A6", a = "#999999", asharp = "#8C8C8C", b = "#D4D4D4"
                )
                ColorPaletteType.CUSTOM -> fallback
            }
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
