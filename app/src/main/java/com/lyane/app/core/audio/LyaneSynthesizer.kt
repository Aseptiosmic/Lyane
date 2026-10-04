package com.lyane.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

class LyaneSynthesizer(private val context: Context) {

    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    ) * 2

    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isRunning = false
    private var renderThread: Thread? = null

    // Polyphonic active voices
    private val activeVoices = ConcurrentHashMap<Int, SynthVoice>()
    private var sustainPedalDown = false

    private class SynthVoice(
        val pitch: Int,
        val velocity: Float,
        val frequency: Float,
        var ageSamples: Long = 0,
        var isKeyReleased: Boolean = false,
        var releaseSamples: Long = 0
    )

    fun start() {
        if (isRunning) return
        isRunning = true

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        renderThread = Thread({ renderLoop() }, "LyaneSynthThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        isRunning = false
        renderThread?.interrupt()
        renderThread = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        activeVoices.clear()
    }

    fun noteOn(pitch: Int, velocity: Int) {
        val freq = midiPitchToFreq(pitch)
        val vel = (velocity / 127.0f).coerceIn(0.1f, 1.0f)
        activeVoices[pitch] = SynthVoice(pitch, vel, freq)
    }

    fun noteOff(pitch: Int) {
        if (sustainPedalDown) {
            // Keep voice ringing until sustain pedal is lifted
            activeVoices[pitch]?.isKeyReleased = true
        } else {
            val voice = activeVoices[pitch]
            if (voice != null) {
                voice.isKeyReleased = true
            }
        }
    }

    fun setSustainPedal(isDown: Boolean) {
        sustainPedalDown = isDown
        if (!isDown) {
            for ((_, voice) in activeVoices) {
                if (voice.isKeyReleased) {
                    voice.releaseSamples = maxOf(voice.releaseSamples, 1L)
                }
            }
        }
    }

    fun allNotesOff() {
        activeVoices.clear()
    }

    private fun renderLoop() {
        val frameCount = 512
        val pcmBuffer = ShortArray(frameCount * 2) // Stereo

        val twoPi = 2.0 * Math.PI

        while (isRunning) {
            pcmBuffer.fill(0)

            if (activeVoices.isNotEmpty()) {
                val deadPitches = mutableListOf<Int>()

                for ((pitch, voice) in activeVoices) {
                    val baseFreq = voice.frequency
                    val vel = voice.velocity

                    for (i in 0 until frameCount) {
                        val t = (voice.ageSamples + i).toDouble() / sampleRate
                        
                        // Acoustic Piano Harmonic Additive Synthesis Model
                        // Piano harmonics decay faster at higher frequencies
                        val h1 = sin(twoPi * baseFreq * t) * exp(-1.2 * t)
                        val h2 = 0.55 * sin(twoPi * baseFreq * 2.002 * t) * exp(-2.4 * t)
                        val h3 = 0.35 * sin(twoPi * baseFreq * 3.006 * t) * exp(-3.8 * t)
                        val h4 = 0.20 * sin(twoPi * baseFreq * 4.01 * t) * exp(-5.0 * t)
                        val h5 = 0.10 * sin(twoPi * baseFreq * 5.015 * t) * exp(-6.5 * t)

                        // Hammer strike transient click on attack
                        val strike = if (t < 0.008) (sin(twoPi * 1800.0 * t) * (1.0 - t / 0.008)) else 0.0

                        var sample = (h1 + h2 + h3 + h4 + h5 + strike * 0.4) * vel

                        // Release envelope
                        if (voice.isKeyReleased && !sustainPedalDown) {
                            val relTime = (voice.releaseSamples + i).toDouble() / sampleRate
                            val env = exp(-18.0 * relTime)
                            sample *= env
                            if (env < 0.001) {
                                deadPitches.add(pitch)
                                break
                            }
                        }

                        // Stereo panning based on pitch (lower pitches to left, higher to right)
                        val pan = ((pitch - 21).toFloat() / 87.0f).coerceIn(0.1f, 0.9f)
                        val leftSample = (sample * (1.0f - pan * 0.4f)).toFloat()
                        val rightSample = (sample * (0.6f + pan * 0.4f)).toFloat()

                        val leftVal = (leftSample * 12000.0f).toInt().coerceIn(-32768, 32767)
                        val rightVal = (rightSample * 12000.0f).toInt().coerceIn(-32768, 32767)

                        pcmBuffer[i * 2] = (pcmBuffer[i * 2] + leftVal).coerceIn(-32768, 32767).toShort()
                        pcmBuffer[i * 2 + 1] = (pcmBuffer[i * 2 + 1] + rightVal).coerceIn(-32768, 32767).toShort()
                    }

                    voice.ageSamples += frameCount
                    if (voice.isKeyReleased && !sustainPedalDown) {
                        voice.releaseSamples += frameCount
                    }
                }

                for (p in deadPitches) {
                    activeVoices.remove(p)
                }
            }

            audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
        }
    }

    private fun midiPitchToFreq(pitch: Int): Float {
        return (440.0 * 2.0.pow((pitch - 69).toDouble() / 12.0)).toFloat()
    }
}
