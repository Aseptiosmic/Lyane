package com.lyane.app.data.model

import java.io.Serializable

enum class TrackType {
    MIDI,
    AUDIO,
    VIDEO_BACKGROUND,
    EFFECT_MARKER,
    CAMERA_AUTOMATION
}

data class TimelineTrack(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var type: TrackType,
    var offsetUs: Long = 0L,
    var durationUs: Long = 0L,
    var isMuted: Boolean = false,
    var volume: Float = 1.0f,
    var opacity: Float = 1.0f,
    var filePath: String? = null
) : Serializable

data class TimelineMarker(
    val id: String = java.util.UUID.randomUUID().toString(),
    var timeUs: Long,
    var name: String,
    var colorHex: String = "#00F2FE",
    var targetPresetId: String? = null
) : Serializable

enum class VideoAspectRatio(val widthRatio: Int, val heightRatio: Int, val label: String) {
    RATIO_16_9(16, 9, "16:9 YouTube / Landscape"),
    RATIO_9_16(9, 16, "9:16 TikTok / Shorts / Reels"),
    RATIO_1_1(1, 1, "1:1 Square / Feed"),
    RATIO_CUSTOM(0, 0, "Custom Resolution")
}

enum class VideoResolution(val width: Int, val height: Int, val label: String) {
    RES_720P(1280, 720, "720p HD"),
    RES_1080P(1920, 1080, "1080p Full HD"),
    RES_1440P(2560, 1440, "1440p 2K QHD"),
    RES_4K(3840, 2160, "4K Ultra HD")
}

data class ExportConfig(
    var aspectRatio: VideoAspectRatio = VideoAspectRatio.RATIO_16_9,
    var resolution: VideoResolution = VideoResolution.RES_1080P,
    var customWidth: Int = 1920,
    var customHeight: Int = 1080,
    var fps: Int = 60,                  // 24, 30, 60
    var bitrateMbps: Int = 18,          // Video bitrate
    var audioBitrateKbps: Int = 256,    // AAC audio bitrate
    var outputPath: String = "",
    var includeAudio: Boolean = true
) : Serializable {

    val targetWidth: Int
        get() = when (aspectRatio) {
            VideoAspectRatio.RATIO_16_9 -> resolution.width
            VideoAspectRatio.RATIO_9_16 -> resolution.height
            VideoAspectRatio.RATIO_1_1 -> minOf(resolution.width, resolution.height)
            VideoAspectRatio.RATIO_CUSTOM -> customWidth
        }

    val targetHeight: Int
        get() = when (aspectRatio) {
            VideoAspectRatio.RATIO_16_9 -> resolution.height
            VideoAspectRatio.RATIO_9_16 -> resolution.width
            VideoAspectRatio.RATIO_1_1 -> minOf(resolution.width, resolution.height)
            VideoAspectRatio.RATIO_CUSTOM -> customHeight
        }
}

data class ProjectData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "Untitled Lyane Project",
    var createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis(),
    var midiFilePath: String? = null,
    var audioFilePath: String? = null,
    var videoBackgroundPath: String? = null,
    var backgroundOpacity: Float = 0.5f,
    var visualConfig: VisualConfig = VisualConfig(),
    var cameraConfig: CameraConfig = CameraConfig(),
    var particleConfig: ParticleConfig = ParticleConfig(),
    var lightingConfig: LightingConfig = LightingConfig(),
    var exportConfig: ExportConfig = ExportConfig(),
    var markers: MutableList<TimelineMarker> = mutableListOf(),
    var activePresetId: String = "preset_moonlight"
) : Serializable
