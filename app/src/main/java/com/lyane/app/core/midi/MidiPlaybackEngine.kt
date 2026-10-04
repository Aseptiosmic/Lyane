package com.lyane.app.core.midi

import com.lyane.app.data.model.MidiNote
import com.lyane.app.data.model.MidiSequence
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MidiEventListener {
    fun onNoteOn(note: MidiNote)
    fun onNoteOff(note: MidiNote)
    fun onPositionChanged(positionUs: Long, progress: Float)
    fun onPlaybackStateChanged(isPlaying: Boolean)
}

class MidiPlaybackEngine {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var playbackJob: Job? = null

    private var currentSequence: MidiSequence? = null

    private val _positionUs = MutableStateFlow(0L)
    val positionUs: StateFlow<Long> = _positionUs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _tempoMultiplier = MutableStateFlow(1.0f)
    val tempoMultiplier: StateFlow<Float> = _tempoMultiplier.asStateFlow()

    var isLooping: Boolean = false
    var loopStartUs: Long = 0L
    var loopEndUs: Long = 0L

    private val listeners = mutableListOf<MidiEventListener>()
    private val activePlayingNotes = mutableSetOf<MidiNote>()

    fun addListener(listener: MidiEventListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: MidiEventListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun setSequence(sequence: MidiSequence?) {
        pause()
        currentSequence = sequence
        _positionUs.value = 0L
        loopStartUs = 0L
        loopEndUs = sequence?.durationUs ?: 0L
        activePlayingNotes.clear()
        notifyPositionChanged(0L)
    }

    fun getSequence(): MidiSequence? = currentSequence

    fun play() {
        val seq = currentSequence ?: return
        if (_isPlaying.value) return

        _isPlaying.value = true
        notifyPlaybackState(true)

        playbackJob = scope.launch {
            var lastTimeNanos = System.nanoTime()

            while (isActive && _isPlaying.value) {
                val nowNanos = System.nanoTime()
                val deltaNanos = nowNanos - lastTimeNanos
                lastTimeNanos = nowNanos

                val deltaUs = ((deltaNanos / 1000) * _tempoMultiplier.value).toLong()
                var newPos = _positionUs.value + deltaUs

                val maxDuration = if (isLooping && loopEndUs > loopStartUs) loopEndUs else (seq.durationUs + 1_000_000L)

                if (newPos >= maxDuration) {
                    if (isLooping) {
                        newPos = loopStartUs
                        stopAllActiveNotes()
                    } else {
                        newPos = seq.durationUs
                        _positionUs.value = newPos
                        notifyPositionChanged(newPos)
                        pause()
                        break
                    }
                }

                _positionUs.value = newPos
                updateActiveNotes(newPos)
                notifyPositionChanged(newPos)

                delay(12) // ~80 Hz sequencer tick
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        stopAllActiveNotes()
        notifyPlaybackState(false)
    }

    fun stop() {
        pause()
        seekTo(0L)
    }

    fun seekTo(targetUs: Long) {
        val seq = currentSequence
        val maxDuration = seq?.durationUs ?: 0L
        val clamped = targetUs.coerceIn(0L, maxOf(0L, maxDuration))
        _positionUs.value = clamped
        stopAllActiveNotes()
        if (seq != null) {
            updateActiveNotes(clamped)
        }
        notifyPositionChanged(clamped)
    }

    fun setTempoMultiplier(multiplier: Float) {
        _tempoMultiplier.value = multiplier.coerceIn(0.25f, 2.5f)
    }

    private fun updateActiveNotes(posUs: Long) {
        val seq = currentSequence ?: return
        val currentNotes = seq.getActiveNotesAt(posUs)

        // Find notes that turned off
        val toRemove = mutableListOf<MidiNote>()
        for (note in activePlayingNotes) {
            if (!currentNotes.contains(note)) {
                toRemove.add(note)
                notifyNoteOff(note)
            }
        }
        activePlayingNotes.removeAll(toRemove.toSet())

        // Find notes that turned on
        for (note in currentNotes) {
            if (!activePlayingNotes.contains(note)) {
                activePlayingNotes.add(note)
                notifyNoteOn(note)
            }
        }
    }

    private fun stopAllActiveNotes() {
        for (note in activePlayingNotes) {
            notifyNoteOff(note)
        }
        activePlayingNotes.clear()
    }

    private fun notifyNoteOn(note: MidiNote) {
        synchronized(listeners) {
            for (l in listeners) {
                l.onNoteOn(note)
            }
        }
    }

    private fun notifyNoteOff(note: MidiNote) {
        synchronized(listeners) {
            for (l in listeners) {
                l.onNoteOff(note)
            }
        }
    }

    private fun notifyPositionChanged(posUs: Long) {
        val duration = currentSequence?.durationUs ?: 1L
        val progress = if (duration > 0) (posUs.toFloat() / duration).coerceIn(0f, 1f) else 0f
        synchronized(listeners) {
            for (l in listeners) {
                l.onPositionChanged(posUs, progress)
            }
        }
    }

    private fun notifyPlaybackState(playing: Boolean) {
        synchronized(listeners) {
            for (l in listeners) {
                l.onPlaybackStateChanged(playing)
            }
        }
    }

    fun release() {
        pause()
        scope.cancel()
    }
}
