package com.lyane.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyane.app.LyaneApplication
import com.lyane.app.core.ai.AiStyleSuggester
import com.lyane.app.core.ai.StyleSuggestion
import com.lyane.app.core.audio.LyaneSynthesizer
import com.lyane.app.core.editor.MidiEditorEngine
import com.lyane.app.core.export.ExportProgressListener
import com.lyane.app.core.export.VideoExporter
import com.lyane.app.core.midi.*
import com.lyane.app.core.musicxml.MusicXmlParser
import com.lyane.app.core.presets.PresetData
import com.lyane.app.core.presets.PresetManager
import com.lyane.app.data.model.*
import com.lyane.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application), MidiEventListener, LiveMidiInputListener {

    private val app = application as LyaneApplication
    private val synthesizer: LyaneSynthesizer = app.synthesizer
    val midiManager: AndroidMidiManager = app.midiManager
    val presetManager: PresetManager = app.presetManager
    val projectRepository: ProjectRepository = app.projectRepository

    val playbackEngine = MidiPlaybackEngine()
    val editorEngine = MidiEditorEngine()
    val videoExporter = VideoExporter(application)
    val aiSuggester = AiStyleSuggester()

    private val midiParser = MidiParser()
    private val musicXmlParser = MusicXmlParser()

    // UI States
    private val _currentSequence = MutableStateFlow<MidiSequence?>(null)
    val currentSequence: StateFlow<MidiSequence?> = _currentSequence.asStateFlow()

    private val _currentProject = MutableStateFlow(ProjectData())
    val currentProject: StateFlow<ProjectData> = _currentProject.asStateFlow()

    private val _visualConfig = MutableStateFlow(VisualConfig())
    val visualConfig: StateFlow<VisualConfig> = _visualConfig.asStateFlow()

    private val _cameraConfig = MutableStateFlow(CameraConfig())
    val cameraConfig: StateFlow<CameraConfig> = _cameraConfig.asStateFlow()

    private val _particleConfig = MutableStateFlow(ParticleConfig())
    val particleConfig: StateFlow<ParticleConfig> = _particleConfig.asStateFlow()

    private val _lightingConfig = MutableStateFlow(LightingConfig())
    val lightingConfig: StateFlow<LightingConfig> = _lightingConfig.asStateFlow()

    private val _exportConfig = MutableStateFlow(ExportConfig())
    val exportConfig: StateFlow<ExportConfig> = _exportConfig.asStateFlow()

    private val _positionUs = MutableStateFlow(0L)
    val positionUs: StateFlow<Long> = _positionUs.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _exportProgress = MutableStateFlow(0)
    val exportProgress: StateFlow<Int> = _exportProgress.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportResultFile = MutableStateFlow<File?>(null)
    val exportResultFile: StateFlow<File?> = _exportResultFile.asStateFlow()

    private val _aiSuggestions = MutableStateFlow<List<StyleSuggestion>>(emptyList())
    val aiSuggestions: StateFlow<List<StyleSuggestion>> = _aiSuggestions.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        playbackEngine.addListener(this)
        midiManager.addListener(this)

        // Load Beethoven Moonlight Sonata as default starter song
        loadDemoSong("moonlight_sonata")
        applyPreset("preset_moonlight")
    }

    fun loadDemoSong(sampleName: String) {
        viewModelScope.launch {
            try {
                val seq = projectRepository.loadSampleMidi(sampleName)
                setSequence(seq)
                _statusMessage.value = "Loaded: ${seq.name}"
            } catch (e: Exception) {
                _statusMessage.value = "Error loading demo: ${e.message}"
            }
        }
    }

    fun loadDemoMusicXml() {
        viewModelScope.launch {
            try {
                val seq = projectRepository.loadSampleMusicXml("clair_de_lune")
                setSequence(seq)
                _statusMessage.value = "Loaded MusicXML: ${seq.name}"
            } catch (e: Exception) {
                _statusMessage.value = "Error loading MusicXML: ${e.message}"
            }
        }
    }

    fun importMidiFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val stream = app.contentResolver.openInputStream(uri)
                if (stream != null) {
                    val bytes = stream.readBytes()
                    val fileName = uri.lastPathSegment ?: "Imported MIDI"
                    val seq = midiParser.parse(bytes, fileName)
                    setSequence(seq)
                    _statusMessage.value = "Successfully imported: ${seq.name}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to import MIDI: ${e.message}"
            }
        }
    }

    fun importMusicXmlFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val stream = app.contentResolver.openInputStream(uri)
                if (stream != null) {
                    val fileName = uri.lastPathSegment ?: "Imported MusicXML"
                    val seq = musicXmlParser.parse(stream, fileName)
                    setSequence(seq)
                    _statusMessage.value = "Successfully imported MusicXML: ${seq.name}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to import MusicXML: ${e.message}"
            }
        }
    }

    fun setSequence(sequence: MidiSequence) {
        playbackEngine.setSequence(sequence)
        _currentSequence.value = sequence
        _positionUs.value = 0L
        _progress.value = 0f

        // Run AI style suggester
        _aiSuggestions.value = aiSuggester.analyzeAndSuggest(sequence)
    }

    fun play() = playbackEngine.play()
    fun pause() = playbackEngine.pause()
    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }
    fun stop() = playbackEngine.stop()
    fun seekTo(positionUs: Long) = playbackEngine.seekTo(positionUs)
    fun seekProgress(frac: Float) {
        val seq = _currentSequence.value ?: return
        val pos = (frac.coerceIn(0f, 1f) * seq.durationUs).toLong()
        seekTo(pos)
    }

    fun setTempoMultiplier(speed: Float) = playbackEngine.setTempoMultiplier(speed)

    fun applyPreset(presetId: String) {
        val preset = presetManager.getPresetById(presetId) ?: return
        _visualConfig.value = _visualConfig.value.copy(
            noteStyle = preset.noteStyle
        )
        _cameraConfig.value = preset.camera
        _particleConfig.value = preset.particles
        _lightingConfig.value = preset.lighting

        val proj = _currentProject.value
        proj.activePresetId = presetId
        _currentProject.value = proj
    }

    fun setNoteStyle(style: NoteStyle) {
        _visualConfig.value = _visualConfig.value.copy(noteStyle = style)
    }

    fun setColorPalette(palette: ColorPaletteType) {
        _visualConfig.value = _visualConfig.value.copy(paletteType = palette)
    }

    fun setCameraTilt(tilt: Float) {
        _cameraConfig.value = _cameraConfig.value.copy(tiltAngle = tilt)
    }

    fun setCameraZoom(zoom: Float) {
        _cameraConfig.value = _cameraConfig.value.copy(zoom = zoom)
    }

    fun setAutoCamera(enabled: Boolean) {
        _cameraConfig.value = _cameraConfig.value.copy(autoCamera = enabled)
    }

    fun setParticlesEnabled(enabled: Boolean) {
        _particleConfig.value = _particleConfig.value.copy(enabled = enabled)
    }

    fun setParticleCount(count: Int) {
        _particleConfig.value = _particleConfig.value.copy(count = count)
    }

    fun setParticleSpeed(speed: Float) {
        _particleConfig.value = _particleConfig.value.copy(speed = speed)
    }

    fun setExportAspectRatio(ratio: VideoAspectRatio) {
        _exportConfig.value = _exportConfig.value.copy(aspectRatio = ratio)
    }

    fun setExportResolution(res: VideoResolution) {
        _exportConfig.value = _exportConfig.value.copy(resolution = res)
    }

    fun setExportFps(fps: Int) {
        _exportConfig.value = _exportConfig.value.copy(fps = fps)
    }

    fun startExport() {
        val seq = _currentSequence.value ?: return
        if (_isExporting.value) return

        _isExporting.value = true
        _exportProgress.value = 0
        _exportResultFile.value = null

        viewModelScope.launch {
            videoExporter.exportVideo(
                sequence = seq,
                visualConfig = _visualConfig.value,
                cameraConfig = _cameraConfig.value,
                particleConfig = _particleConfig.value,
                lightingConfig = _lightingConfig.value,
                exportConfig = _exportConfig.value,
                listener = object : ExportProgressListener {
                    override fun onProgress(percent: Int, currentFrame: Long, totalFrames: Long) {
                        _exportProgress.value = percent
                    }

                    override fun onComplete(outputFile: File) {
                        _isExporting.value = false
                        _exportProgress.value = 100
                        _exportResultFile.value = outputFile
                        _statusMessage.value = "Video successfully rendered to ${outputFile.name}!"
                    }

                    override fun onError(error: Exception) {
                        _isExporting.value = false
                        _statusMessage.value = "Export failed: ${error.message}"
                    }
                }
            )
        }
    }

    fun cancelExport() {
        videoExporter.cancel()
        _isExporting.value = false
    }

    // Playback engine callbacks
    override fun onNoteOn(note: MidiNote) {
        synthesizer.noteOn(note.pitch, note.velocity)
    }

    override fun onNoteOff(note: MidiNote) {
        synthesizer.noteOff(note.pitch)
    }

    override fun onPositionChanged(posUs: Long, progress: Float) {
        _positionUs.value = posUs
        _progress.value = progress
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    // Live MIDI callbacks
    override fun onLiveNoteOn(pitch: Int, velocity: Int) {
        synthesizer.noteOn(pitch, velocity)
    }

    override fun onLiveNoteOff(pitch: Int) {
        synthesizer.noteOff(pitch)
    }

    override fun onLiveControlChange(controller: Int, value: Int) {
        if (controller == 64) { // Sustain pedal
            synthesizer.setSustainPedal(value >= 64)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.removeListener(this)
        midiManager.removeListener(this)
        playbackEngine.release()
    }
}
