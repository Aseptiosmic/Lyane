package com.lyane.app.core.midi

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.lyane.app.data.model.MidiNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface LiveMidiInputListener {
    fun onLiveNoteOn(pitch: Int, velocity: Int)
    fun onLiveNoteOff(pitch: Int)
    fun onLiveControlChange(controller: Int, value: Int)
}

class AndroidMidiManager(private val context: Context) {

    private val midiManager: MidiManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
    } else {
        null
    }

    private val _connectedDevices = MutableStateFlow<List<String>>(emptyList())
    val connectedDevices: StateFlow<List<String>> = _connectedDevices.asStateFlow()

    private val _isDeviceConnected = MutableStateFlow(false)
    val isDeviceConnected: StateFlow<Boolean> = _isDeviceConnected.asStateFlow()

    private val openDevices = mutableListOf<MidiDevice>()
    private val openPorts = mutableListOf<MidiOutputPort>()

    private val listeners = mutableListOf<LiveMidiInputListener>()
    private val handler = Handler(Looper.getMainLooper())

    init {
        refreshDevices()
        registerDeviceCallback()
    }

    fun addListener(listener: LiveMidiInputListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: LiveMidiInputListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun refreshDevices() {
        if (midiManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            _connectedDevices.value = emptyList()
            _isDeviceConnected.value = false
            return
        }

        val infos = midiManager.devices
        val names = infos.map { info ->
            val props = info.properties
            val name = props.getString(MidiDeviceInfo.PROPERTY_NAME)
                ?: props.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                ?: "MIDI Device #${info.id}"
            name
        }
        _connectedDevices.value = names
        _isDeviceConnected.value = names.isNotEmpty()

        // Auto-connect first available device
        if (infos.isNotEmpty() && openDevices.isEmpty()) {
            connectDevice(infos[0])
        }
    }

    private fun registerDeviceCallback() {
        if (midiManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        midiManager.registerDeviceCallback(object : MidiManager.DeviceCallback() {
            override fun onDeviceAdded(device: MidiDeviceInfo?) {
                handler.post { refreshDevices() }
            }

            override fun onDeviceRemoved(device: MidiDeviceInfo?) {
                handler.post { refreshDevices() }
            }
        }, handler)
    }

    fun connectDevice(info: MidiDeviceInfo) {
        if (midiManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        midiManager.openDevice(info, { device ->
            if (device != null) {
                openDevices.add(device)
                for (portInfo in info.ports) {
                    if (portInfo.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                        val outputPort = device.openOutputPort(portInfo.portNumber)
                        if (outputPort != null) {
                            outputPort.connect(createMidiReceiver())
                            openPorts.add(outputPort)
                        }
                    }
                }
            }
        }, handler)
    }

    private fun createMidiReceiver(): MidiReceiver {
        return object : MidiReceiver() {
            override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
                if (msg == null || count < 3) return
                val status = msg[offset].toInt() and 0xFF
                val msgType = status and 0xF0
                val data1 = msg[offset + 1].toInt() and 0x7F
                val data2 = msg[offset + 2].toInt() and 0x7F

                when (msgType) {
                    0x90 -> { // Note On
                        if (data2 > 0) {
                            notifyLiveNoteOn(data1, data2)
                        } else {
                            notifyLiveNoteOff(data1)
                        }
                    }
                    0x80 -> { // Note Off
                        notifyLiveNoteOff(data1)
                    }
                    0xB0 -> { // Control change
                        notifyLiveControlChange(data1, data2)
                    }
                }
            }
        }
    }

    private fun notifyLiveNoteOn(pitch: Int, velocity: Int) {
        handler.post {
            synchronized(listeners) {
                for (l in listeners) l.onLiveNoteOn(pitch, velocity)
            }
        }
    }

    private fun notifyLiveNoteOff(pitch: Int) {
        handler.post {
            synchronized(listeners) {
                for (l in listeners) l.onLiveNoteOff(pitch)
            }
        }
    }

    private fun notifyLiveControlChange(controller: Int, value: Int) {
        handler.post {
            synchronized(listeners) {
                for (l in listeners) l.onLiveControlChange(controller, value)
            }
        }
    }

    // Manual simulation method for on-screen touch keyboard or virtual events
    fun triggerVirtualNoteOn(pitch: Int, velocity: Int = 90) {
        notifyLiveNoteOn(pitch, velocity)
    }

    fun triggerVirtualNoteOff(pitch: Int) {
        notifyLiveNoteOff(pitch)
    }

    fun release() {
        for (p in openPorts) {
            try { p.close() } catch (_: Exception) {}
        }
        openPorts.clear()

        for (d in openDevices) {
            try { d.close() } catch (_: Exception) {}
        }
        openDevices.clear()
    }
}
