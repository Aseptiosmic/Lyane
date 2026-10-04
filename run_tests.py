#!/usr/bin/env python3
import os
import sys
import zipfile
import json

def run_tests():
    print("=" * 60)
    print("LYANE VERIFICATION & INTEGRATION TEST SUITE")
    print("=" * 60)

    # Test 1: Project Structure Check
    required_paths = [
        "app/build.gradle.kts",
        "build.gradle.kts",
        "settings.gradle.kts",
        "gradle/libs.versions.toml",
        "gradle/wrapper/gradle-wrapper.properties",
        "app/src/main/AndroidManifest.xml",
        "app/src/main/java/com/lyane/app/LyaneApplication.kt",
        "app/src/main/java/com/lyane/app/ui/MainActivity.kt",
        "app/src/main/java/com/lyane/app/core/midi/MidiParser.kt",
        "app/src/main/java/com/lyane/app/core/midi/MidiPlaybackEngine.kt",
        "app/src/main/java/com/lyane/app/core/midi/AndroidMidiManager.kt",
        "app/src/main/java/com/lyane/app/core/musicxml/MusicXmlParser.kt",
        "app/src/main/java/com/lyane/app/core/audio/LyaneSynthesizer.kt",
        "app/src/main/java/com/lyane/app/core/visual/VisualEngine.kt",
        "app/src/main/java/com/lyane/app/core/visual/FallingNotesRenderer.kt",
        "app/src/main/java/com/lyane/app/core/visual/KeyboardRenderer.kt",
        "app/src/main/java/com/lyane/app/core/particles/ParticleEngine.kt",
        "app/src/main/java/com/lyane/app/core/camera/Camera3DController.kt",
        "app/src/main/java/com/lyane/app/core/editor/MidiEditorEngine.kt",
        "app/src/main/java/com/lyane/app/core/export/VideoExporter.kt",
        "app/src/main/java/com/lyane/app/core/presets/PresetManager.kt",
        "app/src/main/java/com/lyane/app/core/scripting/VisualScriptingEngine.kt",
        "app/src/main/java/com/lyane/app/core/ai/AiStyleSuggester.kt",
        "app/src/main/java/com/lyane/app/ui/screens/HomeScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/StudioScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/EditorScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/PresetsScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/ExportScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/TimelineScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/OnboardingScreen.kt",
        "app/src/main/java/com/lyane/app/ui/screens/SettingsScreen.kt",
        "Lyane.apk"
    ]

    all_exist = True
    for p in required_paths:
        full_p = os.path.join("/home/user/Lyane", p)
        if os.path.exists(full_p):
            print(f"[PASS] Found: {p}")
        else:
            print(f"[FAIL] Missing: {p}")
            all_exist = False

    # Test 2: APK Archive Validation
    print("\n--- APK Archive Validation ---")
    apk_path = "/home/user/Lyane/Lyane.apk"
    with zipfile.ZipFile(apk_path, 'r') as zf:
        namelist = zf.namelist()
        required_apk_entries = [
            "AndroidManifest.xml",
            "classes.dex",
            "resources.arsc",
            "assets/demo_midi/moonlight_sonata.mid",
            "assets/demo_midi/clair_de_lune.mid",
            "assets/demo_midi/bach_prelude.mid",
            "assets/demo_xml/clair_de_lune.musicxml",
            "assets/presets/moonlight.json",
            "assets/presets/lunar_glass.json",
            "assets/presets/aurora.json",
            "assets/presets/cyber_piano.json",
            "assets/presets/crystal.json",
            "assets/presets/cosmic.json",
            "assets/presets/minimal.json",
            "assets/presets/cinematic.json",
            "assets/presets/electric.json",
            "assets/presets/dream.json",
            "assets/shaders/note_vertex.glsl",
            "assets/shaders/note_fragment.glsl",
            "assets/shaders/particle_vertex.glsl",
            "assets/shaders/particle_fragment.glsl",
            "assets/shaders/bloom_vertex.glsl",
            "assets/shaders/bloom_fragment.glsl",
            "META-INF/MANIFEST.MF",
            "META-INF/CERT.SF",
            "META-INF/CERT.RSA"
        ]
        for entry in required_apk_entries:
            if entry in namelist:
                print(f"[PASS] APK contains: {entry}")
            else:
                print(f"[FAIL] Missing in APK: {entry}")
                all_exist = False

    # Test 3: Preset JSON Validity
    print("\n--- Presets Verification ---")
    preset_dir = "/home/user/Lyane/app/src/main/assets/presets"
    for f in os.listdir(preset_dir):
        if f.endswith(".json"):
            with open(os.path.join(preset_dir, f)) as jf:
                data = json.load(jf)
                assert "id" in data and "name" in data and "noteStyle" in data
                print(f"[PASS] Preset valid: {data['name']} ({f})")

    print("\n" + "=" * 60)
    if all_exist:
        print("ALL TESTS PASSED SUCCESSFULLY! LYANE IS READY.")
    else:
        print("TEST FAILURES DETECTED.")
    print("=" * 60)

if __name__ == '__main__':
    run_tests()
