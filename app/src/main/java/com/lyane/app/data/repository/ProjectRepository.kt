package com.lyane.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.lyane.app.core.midi.MidiParser
import com.lyane.app.core.musicxml.MusicXmlParser
import com.lyane.app.data.model.*
import java.io.File

class ProjectRepository(private val context: Context) {

    private val gson = Gson()
    private val midiParser = MidiParser()
    private val musicXmlParser = MusicXmlParser()

    private val projectsDir = File(context.filesDir, "projects").apply { mkdirs() }
    private val sampleMidiDir = File(context.filesDir, "samples").apply { mkdirs() }

    fun getRecentProjects(): List<ProjectData> {
        val list = mutableListOf<ProjectData>()
        val files = projectsDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
        for (f in files) {
            try {
                f.bufferedReader().use { reader ->
                    val proj = gson.fromJson(reader, ProjectData::class.java)
                    if (proj != null) list.add(proj)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list.sortedByDescending { it.modifiedAt }
    }

    fun saveProject(project: ProjectData) {
        project.modifiedAt = System.currentTimeMillis()
        val file = File(projectsDir, "${project.id}.json")
        file.writeText(gson.toJson(project))
    }

    fun loadProject(id: String): ProjectData? {
        val file = File(projectsDir, "$id.json")
        if (!file.exists()) return null
        return try {
            file.bufferedReader().use { gson.fromJson(it, ProjectData::class.java) }
        } catch (_: Exception) { null }
    }

    fun loadSampleMidi(sampleName: String): MidiSequence {
        // Try assets first
        return try {
            context.assets.open("demo_midi/$sampleName.mid").use { stream ->
                midiParser.parse(stream, sampleName.replace('_', ' ').capitalize())
            }
        } catch (e: Exception) {
            // Fallback synthetic sequence
            createFallbackSequence(sampleName)
        }
    }

    fun loadSampleMusicXml(sampleName: String = "clair_de_lune"): MidiSequence {
        return try {
            context.assets.open("demo_xml/$sampleName.musicxml").use { stream ->
                musicXmlParser.parse(stream, "Clair de Lune (MusicXML)")
            }
        } catch (e: Exception) {
            loadSampleMidi("clair_de_lune")
        }
    }

    private fun createFallbackSequence(name: String): MidiSequence {
        val seq = MidiSequence(name = name, ppq = 480)
        val track = MidiTrack(index = 0, name = "Piano")
        // Basic C Major arpeggios
        val notes = listOf(60, 64, 67, 72, 76, 79, 84)
        for (i in notes.indices) {
            track.notes.add(
                MidiNote(
                    pitch = notes[i],
                    startTimeUs = i * 400_000L,
                    durationUs = 600_000L,
                    velocity = 80 + i * 4
                )
            )
        }
        seq.tracks.add(track)
        return seq
    }
}
