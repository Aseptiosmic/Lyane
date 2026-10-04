# Lyane Proguard Rules
-keep class com.lyane.app.data.model.** { *; }
-keep class com.lyane.app.core.midi.** { *; }
-keep class com.lyane.app.core.musicxml.** { *; }
-keep class com.lyane.app.core.presets.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
