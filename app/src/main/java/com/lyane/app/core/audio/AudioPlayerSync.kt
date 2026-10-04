package com.lyane.app.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

class AudioPlayerSync(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    fun loadAudio(uriOrPath: String) {
        release()
        try {
            mediaPlayer = MediaPlayer().apply {
                if (uriOrPath.startsWith("content://") || uriOrPath.startsWith("android.resource://")) {
                    setDataSource(context, Uri.parse(uriOrPath))
                } else {
                    setDataSource(uriOrPath)
                }
                prepare()
                isPrepared = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPrepared = false
        }
    }

    fun play() {
        if (isPrepared) {
            mediaPlayer?.start()
        }
    }

    fun pause() {
        if (isPrepared && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun seekTo(positionMs: Long) {
        if (isPrepared) {
            mediaPlayer?.seekTo(positionMs.toInt())
        }
    }

    fun setVolume(volume: Float) {
        val v = volume.coerceIn(0.0f, 1.0f)
        mediaPlayer?.setVolume(v, v)
    }

    fun release() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isPrepared = false
    }
}
