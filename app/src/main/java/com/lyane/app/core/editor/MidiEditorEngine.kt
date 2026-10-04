package com.lyane.app.core.editor

import com.lyane.app.data.model.*
import java.util.Stack

sealed class EditorCommand {
    data class AddNote(val trackIndex: Int, val note: MidiNote) : EditorCommand()
    data class DeleteNote(val trackIndex: Int, val note: MidiNote) : EditorCommand()
    data class MoveNote(val trackIndex: Int, val note: MidiNote, val oldPitch: Int, val oldTimeUs: Long, val newPitch: Int, val newTimeUs: Long) : EditorCommand()
    data class ResizeNote(val trackIndex: Int, val note: MidiNote, val oldDurationUs: Long, val newDurationUs: Long) : EditorCommand()
    data class ChangeVelocity(val trackIndex: Int, val note: MidiNote, val oldVel: Int, val newVel: Int) : EditorCommand()
}

class MidiEditorEngine {

    private val undoStack = Stack<EditorCommand>()
    private val redoStack = Stack<EditorCommand>()

    var currentTrackIndex: Int = 0
    var quantizeGridTicks: Int = 120 // 1/16 note at 480 PPQ

    fun addNote(sequence: MidiSequence, trackIndex: Int, pitch: Int, startTimeUs: Long, durationUs: Long, velocity: Int = 80): MidiNote {
        val note = MidiNote(
            pitch = pitch.coerceIn(21, 108),
            startTimeUs = maxOf(0L, startTimeUs),
            durationUs = maxOf(20_000L, durationUs),
            velocity = velocity.coerceIn(1, 127),
            trackIndex = trackIndex
        )
        val track = sequence.tracks.getOrNull(trackIndex) ?: sequence.tracks.firstOrNull()
        track?.notes?.add(note)
        track?.notes?.sortBy { it.startTimeUs }

        undoStack.push(EditorCommand.AddNote(trackIndex, note))
        redoStack.clear()
        return note
    }

    fun deleteNote(sequence: MidiSequence, note: MidiNote) {
        val track = sequence.tracks.getOrNull(note.trackIndex) ?: return
        track.notes.remove(note)
        undoStack.push(EditorCommand.DeleteNote(note.trackIndex, note))
        redoStack.clear()
    }

    fun moveNote(sequence: MidiSequence, note: MidiNote, newPitch: Int, newTimeUs: Long) {
        val oldPitch = note.pitch
        val oldTime = note.startTimeUs

        note.pitch = newPitch.coerceIn(21, 108)
        note.startTimeUs = maxOf(0L, newTimeUs)

        val track = sequence.tracks.getOrNull(note.trackIndex)
        track?.notes?.sortBy { it.startTimeUs }

        undoStack.push(EditorCommand.MoveNote(note.trackIndex, note, oldPitch, oldTime, newPitch, newTimeUs))
        redoStack.clear()
    }

    fun resizeNote(sequence: MidiSequence, note: MidiNote, newDurationUs: Long) {
        val oldDur = note.durationUs
        note.durationUs = maxOf(20_000L, newDurationUs)

        undoStack.push(EditorCommand.ResizeNote(note.trackIndex, note, oldDur, note.durationUs))
        redoStack.clear()
    }

    fun changeVelocity(sequence: MidiSequence, note: MidiNote, newVel: Int) {
        val oldVel = note.velocity
        note.velocity = newVel.coerceIn(1, 127)

        undoStack.push(EditorCommand.ChangeVelocity(note.trackIndex, note, oldVel, note.velocity))
        redoStack.clear()
    }

    fun quantizeTrack(sequence: MidiSequence, trackIndex: Int) {
        val track = sequence.tracks.getOrNull(trackIndex) ?: return
        val ppq = sequence.ppq
        val tempo = sequence.tempoEvents.firstOrNull()?.usPerQuarter ?: 500_000L
        val gridUs = (quantizeGridTicks.toDouble() / ppq * tempo).toLong()

        for (note in track.notes) {
            val nearestGrid = ((note.startTimeUs + gridUs / 2) / gridUs) * gridUs
            note.startTimeUs = maxOf(0L, nearestGrid)
        }
        track.notes.sortBy { it.startTimeUs }
    }

    fun undo(sequence: MidiSequence): Boolean {
        if (undoStack.isEmpty()) return false
        val cmd = undoStack.pop()
        redoStack.push(cmd)

        when (cmd) {
            is EditorCommand.AddNote -> {
                sequence.tracks.getOrNull(cmd.trackIndex)?.notes?.remove(cmd.note)
            }
            is EditorCommand.DeleteNote -> {
                val track = sequence.tracks.getOrNull(cmd.trackIndex)
                track?.notes?.add(cmd.note)
                track?.notes?.sortBy { it.startTimeUs }
            }
            is EditorCommand.MoveNote -> {
                cmd.note.pitch = cmd.oldPitch
                cmd.note.startTimeUs = cmd.oldTimeUs
                sequence.tracks.getOrNull(cmd.trackIndex)?.notes?.sortBy { it.startTimeUs }
            }
            is EditorCommand.ResizeNote -> {
                cmd.note.durationUs = cmd.oldDurationUs
            }
            is EditorCommand.ChangeVelocity -> {
                cmd.note.velocity = cmd.oldVel
            }
        }
        return true
    }

    fun redo(sequence: MidiSequence): Boolean {
        if (redoStack.isEmpty()) return false
        val cmd = redoStack.pop()
        undoStack.push(cmd)

        when (cmd) {
            is EditorCommand.AddNote -> {
                val track = sequence.tracks.getOrNull(cmd.trackIndex)
                track?.notes?.add(cmd.note)
                track?.notes?.sortBy { it.startTimeUs }
            }
            is EditorCommand.DeleteNote -> {
                sequence.tracks.getOrNull(cmd.trackIndex)?.notes?.remove(cmd.note)
            }
            is EditorCommand.MoveNote -> {
                cmd.note.pitch = cmd.newPitch
                cmd.note.startTimeUs = cmd.newTimeUs
                sequence.tracks.getOrNull(cmd.trackIndex)?.notes?.sortBy { it.startTimeUs }
            }
            is EditorCommand.ResizeNote -> {
                cmd.note.durationUs = cmd.newDurationUs
            }
            is EditorCommand.ChangeVelocity -> {
                cmd.note.velocity = cmd.newVel
            }
        }
        return true
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}
