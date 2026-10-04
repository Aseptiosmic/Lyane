package com.lyane.app

import com.lyane.app.core.ai.AiStyleSuggester
import com.lyane.app.core.camera.Camera3DController
import com.lyane.app.core.editor.MidiEditorEngine
import com.lyane.app.core.midi.MidiParser
import com.lyane.app.core.midi.MidiSerializer
import com.lyane.app.core.musicxml.MusicXmlParser
import com.lyane.app.core.visual.KeyboardRenderer
import com.lyane.app.data.model.*
import org.junit.Assert.*
import org.junit.Test

class LyaneUnitTest {

    @Test
    fun testMidiParserAndSerializerRoundtrip() {
        val original = MidiSequence(name = "Test Track", ppq = 480)
        val track = MidiTrack(index = 0, name = "Piano")
        track.notes.add(MidiNote(pitch = 60, startTimeUs = 0L, durationUs = 500_000L, velocity = 90))
        track.notes.add(MidiNote(pitch = 64, startTimeUs = 500_000L, durationUs = 500_000L, velocity = 85))
        track.notes.add(MidiNote(pitch = 67, startTimeUs = 1_000_000L, durationUs = 1_000_000L, velocity = 95))
        original.tracks.add(track)

        val serializer = MidiSerializer()
        val midiBytes = serializer.serialize(original)
        assertNotNull(midiBytes)
        assertTrue(midiBytes.size > 20)

        val parser = MidiParser()
        val parsed = parser.parse(midiBytes, "Roundtrip Track")
        assertNotNull(parsed)
        assertEquals(1, parsed.tracks.size)
        assertEquals(3, parsed.tracks[0].notes.size)
        assertEquals(60, parsed.tracks[0].notes[0].pitch)
        assertEquals(64, parsed.tracks[0].notes[1].pitch)
        assertEquals(67, parsed.tracks[0].notes[2].pitch)
    }

    @Test
    fun testMusicXmlParser() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <score-partwise version="3.1">
              <work><work-title>Test Piece</work-title></work>
              <part id="P1">
                <measure number="1">
                  <attributes><divisions>4</divisions></attributes>
                  <sound tempo="120"/>
                  <note>
                    <pitch><step>C</step><octave>4</octave></pitch>
                    <duration>4</duration>
                  </note>
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        val parser = MusicXmlParser()
        val seq = parser.parseString(sampleXml, "Test XML")
        assertNotNull(seq)
        assertEquals("Test Piece", seq.name)
        assertEquals(1, seq.tracks[0].notes.size)
        assertEquals(60, seq.tracks[0].notes[0].pitch) // C4 = MIDI 60
    }

    @Test
    fun testMidiEditorEngine() {
        val engine = MidiEditorEngine()
        val seq = MidiSequence(name = "Editor Test", ppq = 480)
        val track = MidiTrack(index = 0, name = "Piano")
        seq.tracks.add(track)

        // 1. Add Note
        val note = engine.addNote(seq, 0, 60, 0L, 500_000L, 80)
        assertEquals(1, track.notes.size)

        // 2. Move Note
        engine.moveNote(seq, note, 62, 100_000L)
        assertEquals(62, note.pitch)
        assertEquals(100_000L, note.startTimeUs)

        // 3. Undo
        val undone = engine.undo(seq)
        assertTrue(undone)
        assertEquals(60, note.pitch)
        assertEquals(0L, note.startTimeUs)
    }

    @Test
    fun testAiStyleSuggester() {
        val suggester = AiStyleSuggester()
        val seq = MidiSequence(name = "Fast Piece", ppq = 480)
        val track = MidiTrack(index = 0)
        // Add 20 rapid notes
        for (i in 0 until 20) {
            track.notes.add(MidiNote(pitch = 60 + i, startTimeUs = i * 100_000L, durationUs = 80_000L, velocity = 100))
        }
        seq.tracks.add(track)

        val suggestions = suggester.analyzeAndSuggest(seq)
        assertFalse(suggestions.isEmpty())
        assertTrue(suggestions[0].matchScore > 80)
    }

    @Test
    fun test3DCameraProjection() {
        val camera = Camera3DController()
        val (screenX, screenY) = camera.projectPoint(
            normX = 0.0f,
            normY = 0.5f,
            screenWidth = 1920f,
            screenHeight = 1080f,
            keyboardY = 900f
        )
        assertTrue(screenX > 0f && screenX < 1920f)
        assertTrue(screenY > 0f && screenY < 900f)
    }

    @Test
    fun testKeyboardPitchMapping() {
        val renderer = KeyboardRenderer()
        val config = VisualConfig(keyCount = 88, firstKeyPitch = 21)

        val normA0 = renderer.getPitchNormX(21, config)
        val normC8 = renderer.getPitchNormX(108, config)
        val normMid = renderer.getPitchNormX(60, config)

        assertEquals(-1.0f, normA0, 0.05f)
        assertEquals(1.0f, normC8, 0.05f)
        assertTrue(normMid > -0.5f && normMid < 0.5f)
    }
}
