package com.lyane.app.core.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.lyane.app.core.visual.VisualEngine
import com.lyane.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

interface ExportProgressListener {
    fun onProgress(percent: Int, currentFrame: Long, totalFrames: Long)
    fun onComplete(outputFile: File)
    fun onError(error: Exception)
}

class VideoExporter(private val context: Context) {

    private var isCancelled = false

    fun cancel() {
        isCancelled = true
    }

    suspend fun exportVideo(
        sequence: MidiSequence,
        visualConfig: VisualConfig,
        cameraConfig: CameraConfig,
        particleConfig: ParticleConfig,
        lightingConfig: LightingConfig,
        exportConfig: ExportConfig,
        listener: ExportProgressListener
    ) = withContext(Dispatchers.Default) {
        isCancelled = false
        val width = exportConfig.targetWidth
        val height = exportConfig.targetHeight
        val fps = exportConfig.fps
        val durationUs = sequence.durationUs + 1_500_000L // 1.5s padding at end

        val totalFrames = (durationUs * fps / 1_000_000L).coerceAtLeast(30L)
        val frameDurationUs = 1_000_000L / fps

        val outputFile = if (exportConfig.outputPath.isNotBlank()) {
            File(exportConfig.outputPath)
        } else {
            val dir = File(context.getExternalFilesDir(null), "renders").apply { mkdirs() }
            File(dir, "lyane_render_${System.currentTimeMillis()}.mp4")
        }

        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var videoTrackIndex = -1
        var muxerStarted = false

        val visualEngine = VisualEngine(context)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, exportConfig.bitrateMbps * 1024 * 1024)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val bufferInfo = MediaCodec.BufferInfo()
            val yuvBuffer = ByteArray(width * height * 3 / 2)

            for (frame in 0 until totalFrames) {
                if (isCancelled) {
                    throw InterruptedException("Export cancelled by user")
                }

                val currentUs = frame * frameDurationUs

                // Trigger active note hits for particle emission
                val activeNotes = sequence.getActiveNotesAt(currentUs)
                val keyboardHeight = height * visualConfig.keyHeightRatio
                val keyboardY = height - keyboardHeight

                for (note in activeNotes) {
                    if (currentUs in note.startTimeUs..(note.startTimeUs + frameDurationUs)) {
                        visualEngine.onNoteOn(note, visualConfig, particleConfig, width.toFloat(), height.toFloat(), keyboardY)
                    }
                }

                // Render visual frame
                visualEngine.renderFrame(
                    canvas,
                    width.toFloat(),
                    height.toFloat(),
                    sequence,
                    currentUs,
                    visualConfig,
                    cameraConfig,
                    particleConfig,
                    lightingConfig
                )

                // Convert Bitmap to NV21/YUV420
                bitmapToYuv420(bitmap, yuvBuffer, width, height)

                // Send to MediaCodec input buffer
                val inputIndex = encoder.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(yuvBuffer)
                    encoder.queueInputBuffer(inputIndex, 0, yuvBuffer.size, currentUs, 0)
                }

                // Drain output buffers
                var outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
                while (outputIndex >= 0) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size != 0) {
                        if (!muxerStarted) {
                            val newFormat = encoder.outputFormat
                            videoTrackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            muxerStarted = true
                        }

                        val encodedData = encoder.getOutputBuffer(outputIndex)
                        if (encodedData != null) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                    }

                    encoder.releaseOutputBuffer(outputIndex, false)
                    outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
                }

                // Notify progress
                val percent = ((frame + 1) * 100 / totalFrames).toInt()
                withContext(Dispatchers.Main) {
                    listener.onProgress(percent, frame + 1, totalFrames)
                }
            }

            // Signal End of Stream
            val inputIndex = encoder.dequeueInputBuffer(10_000)
            if (inputIndex >= 0) {
                encoder.queueInputBuffer(inputIndex, 0, 0, durationUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }

            // Drain remaining
            var outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            while (outputIndex >= 0) {
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
                if (bufferInfo.size != 0 && muxerStarted) {
                    val encodedData = encoder.getOutputBuffer(outputIndex)
                    if (encodedData != null) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outputIndex, false)
                outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            }

            withContext(Dispatchers.Main) {
                listener.onComplete(outputFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                listener.onError(e)
            }
        } finally {
            try { encoder?.stop() } catch (_: Exception) {}
            try { encoder?.release() } catch (_: Exception) {}
            try {
                if (muxerStarted) muxer?.stop()
                muxer?.release()
            } catch (_: Exception) {}
            bitmap.recycle()
            visualEngine.clear()
        }
    }

    private fun bitmapToYuv420(bitmap: Bitmap, yuv: ByteArray, width: Int, height: Int) {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[j * width + i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                // RGB to YUV standard formulas
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = y.coerceIn(16, 235).toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv[uvIndex++] = u.coerceIn(16, 240).toByte()
                    yuv[uvIndex++] = v.coerceIn(16, 240).toByte()
                }
            }
        }
    }
}
