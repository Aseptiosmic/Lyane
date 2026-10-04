package com.lyane.app

import android.app.Application
import com.lyane.app.core.audio.LyaneSynthesizer
import com.lyane.app.core.midi.AndroidMidiManager
import com.lyane.app.core.presets.PresetManager
import com.lyane.app.data.repository.ProjectRepository

class LyaneApplication : Application() {

    lateinit var synthesizer: LyaneSynthesizer
        private set

    lateinit var midiManager: AndroidMidiManager
        private set

    lateinit var presetManager: PresetManager
        private set

    lateinit var projectRepository: ProjectRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        synthesizer = LyaneSynthesizer(this)
        synthesizer.start()

        midiManager = AndroidMidiManager(this)
        presetManager = PresetManager(this)
        projectRepository = ProjectRepository(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        synthesizer.stop()
        midiManager.release()
    }

    companion object {
        lateinit var instance: LyaneApplication
            private set
    }
}
