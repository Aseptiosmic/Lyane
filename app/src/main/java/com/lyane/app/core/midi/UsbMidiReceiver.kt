package com.lyane.app.core.midi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import com.lyane.app.LyaneApplication

class UsbMidiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == UsbManager.ACTION_USB_DEVICE_ATTACHED ||
            action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
            LyaneApplication.instance.midiManager.refreshDevices()
        }
    }
}
