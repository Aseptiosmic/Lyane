package com.lyane.app.core.ai

import com.lyane.app.data.model.MidiSequence

data class StyleSuggestion(
    val presetId: String,
    val presetName: String,
    val matchScore: Int, // 0..100%
    val reason: String
)

class AiStyleSuggester {

    fun analyzeAndSuggest(sequence: MidiSequence): List<StyleSuggestion> {
        val totalNotes = sequence.totalNoteCount
        val durationSec = sequence.durationUs / 1_000_000.0
        val noteDensity = if (durationSec > 0) totalNotes / durationSec else 0.0
        val bpm = sequence.initialBpm

        val allNotes = sequence.allNotes
        val avgVelocity = if (allNotes.isNotEmpty()) allNotes.map { it.velocity }.average() else 70.0
        val pitchRange = if (allNotes.isNotEmpty()) (allNotes.maxOf { it.pitch } - allNotes.minOf { it.pitch }) else 40

        val suggestions = mutableListOf<StyleSuggestion>()

        // 1. Fast & Energetic -> Cyber Piano / Electric
        if (noteDensity > 8.0 || bpm > 130) {
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_cyber_piano",
                    presetName = "Cyber Piano",
                    matchScore = 96,
                    reason = "High tempo ($bpm BPM) and dense rhythmic notes (${"%.1f".format(noteDensity)} notes/sec) create an electric synthwave pulse."
                )
            )
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_electric",
                    presetName = "Electric Arc",
                    matchScore = 88,
                    reason = "Fast arpeggios trigger high-voltage lightning crackles."
                )
            )
        }

        // 2. Slow, lyrical, nocturne -> Moonlight / Lunar Glass / Dream
        if (bpm < 80 || noteDensity < 4.0) {
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_moonlight",
                    presetName = "Moonlight",
                    matchScore = 98,
                    reason = "Introspective tempo ($bpm BPM) pairs perfectly with deep lunar cyan falling beams."
                )
            )
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_lunar_glass",
                    presetName = "Lunar Glass",
                    matchScore = 92,
                    reason = "Ethereal acoustic sustain brings out frosted glass reflections."
                )
            )
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_dream",
                    presetName = "Dream Cloud",
                    matchScore = 85,
                    reason = "Soft lyrical lines blend into pastel celestial twilight."
                )
            )
        }

        // 3. Wide harmonic range -> Aurora / Cosmic / Crystal
        if (pitchRange > 50 || avgVelocity > 80) {
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_aurora",
                    presetName = "Aurora",
                    matchScore = 94,
                    reason = "Wide pitch spectrum (${pitchRange} semitones) activates full emerald-teal polar light curtains."
                )
            )
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_cosmic",
                    presetName = "Cosmic Stardust",
                    matchScore = 90,
                    reason = "Dynamic velocity peaks create brilliant supernova particle bursts."
                )
            )
        }

        // Fallback default suggestions if list is small
        if (suggestions.isEmpty()) {
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_moonlight",
                    presetName = "Moonlight",
                    matchScore = 95,
                    reason = "Signature Lyane aesthetic designed for balanced piano visualization."
                )
            )
            suggestions.add(
                StyleSuggestion(
                    presetId = "preset_crystal",
                    presetName = "Crystal Gem",
                    matchScore = 88,
                    reason = "Clean prismatic reflections for clear polyphonic visibility."
                )
            )
        }

        return suggestions.sortedByDescending { it.matchScore }
    }
}
