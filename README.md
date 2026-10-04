# 🌙 LYANE — Professional Android Piano MIDI Visualizer & Video Creator

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-00F2FE.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Kotlin_|_Jetpack_Compose_|_MVVM-8B5CF6.svg)]()
[![Rendering](https://img.shields.io/badge/Engine-3D_GPU_Shaders_|_OpenGL_ES_3.0-10B981.svg)]()
[![Export](https://img.shields.io/badge/Render-Offline_4K_60FPS_MP4-EC4899.svg)]()
[![License](https://img.shields.io/badge/License-100%25_Free_|_No_Watermark-F59E0B.svg)]()

> **Lyane** (inspired by *Luna* — the moon, celestial radiance, and nocturnal harmony) is a next-generation Android application for pianists, content creators, and visual artists. It imports MIDI and MusicXML files, connects to real digital pianos and USB/Bluetooth MIDI keyboards with ultra-low latency, transforms keystrokes into 3D falling-note visual animations with physics-based particles and bloom, and renders cinema-grade MP4 videos offline directly on your mobile device.

---

## 💎 Brand & Visual Identity

- **Brand Name**: `Lyane`
- **Aesthetic**: Modern, premium, technological, musical, cinematic, minimalist, mysterious, and elegant.
- **Color Palette**:
  - `Midnight Eclipse`: `#07080D`
  - `Deep Lunar Surface`: `#0F111A`
  - `Lunar Cyan Core`: `#00F2FE` (Glow: `#4FACFE`)
  - `Celestial Purple`: `#8B5CF6`
  - `Neon Aurora`: `#10B981`
  - `Electric Magenta`: `#EC4899`
- **Logo Emblem**: A geometric lunar eclipse with concentric orbital resonance arcs and radiant piano light beams. 100% original design.

---

## 🚀 Key Features

### 1. 🎹 Comprehensive MIDI Engine
- Full SMF (Standard MIDI File) Format 0 and Format 1 parsing and writing.
- Note On / Note Off, polyphonic tracking, velocity sensitivity (1–127), microsecond timing precision.
- CC 64 Sustain Pedal damper simulation, Pitch Bend, and Channel routing.
- Real-time transport controls: Play, Pause, Seek, Loop, Tempo scaling ($0.25\times$ to $2.0\times$).
- Multi-track solo and mute capability.

### 2. 🎼 MusicXML Compatibility
- Full MusicXML 3.1 `<score-partwise>` parser.
- Extracts parts, measures, divisions, key signatures, time signatures, tempos, notes, chords, and ties.
- Seamless conversion to synchronized visual falling-note timelines.

### 3. ⚡ Live Piano & Low-Latency MIDI Mode
- Native Android `android.media.midi.MidiManager` USB OTG and Bluetooth LE MIDI input.
- Real-time touch keyboard and hardware keyboard responsiveness.
- Instantaneous falling notes, key press illumination, and dynamic particle bursts upon key strikes.

### 4. 🎨 3D GPU-Accelerated Visual Engine
- **3D Perspective Camera**: Adjustable perspective tilt angle ($0^\circ$–$75^\circ$), field of view ($30^\circ$–$85^\circ$), zoom, and horizontal/vertical pan.
- **Dynamic Auto-Camera**: Automatically shifts perspective, tilt, and zoom in response to the musical energy and chord density.
- **10 Note Shader Styles**:
  1. `Classic`: Clean, high-contrast falling rectangles.
  2. `Neon`: Glowing neon core with bright illuminated spines and bloom.
  3. `Glass`: Frosted translucent refractive glass with edge highlights.
  4. `Crystal`: Faceted prismatic diamond geometry.
  5. `Gem`: Specular sheen and caustics.
  6. `Particle`: Particle-dense glowing streams.
  7. `Energy`: Electric plasma wave oscillations.
  8. `Wave`: Subtle spatial harmonic displacement.
  9. `Minimal`: Flat ultra-modern monochrome lines.
  10. `Cosmic`: Stardust nebulae with internal stellar speckles.

### 5. ✨ Physics-Based Particle Engine
- Real-time simulation of velocity-reactive particles.
- Velocity-scaled burst count, speed, radial spread angle, gravity, lifetime, turbulence, and opacity.
- Dynamic chord explosion bursts when 3+ notes hit simultaneously.

### 6. 🌈 12-Pitch Chroma Color System
- Distinct color mapping for all 12 musical pitch classes (C, C♯, D, D♯, E, F, F♯, G, G♯, A, A♯, B).
- Built-in palettes: *Moonlight*, *Aurora*, *Neon*, *Ocean*, *Sunset*, *Galaxy*, *Monochrome*, and *Custom*.

### 7. 🎬 Offline Hardware-Accelerated Video Exporter
- Direct frame-by-frame rendering pipeline via Android `MediaCodec` (H.264/AVC) + `MediaMuxer`.
- **Target Aspect Ratios**:
  - `16:9` (YouTube / Landscape)
  - `9:16` (TikTok / YouTube Shorts / Instagram Reels)
  - `1:1` (Square Instagram / Feed)
  - `Custom`
- **Resolutions**: `720p HD`, `1080p Full HD`, `1440p 2K`, `4K Ultra HD`.
- **Framerate Options**: `24 FPS`, `30 FPS`, `60 FPS`.
- 100% offline rendering with progress reporting, zero server uploads, and no watermarks!

### 8. 🎹 Built-in Polyphonic Synthesizer
- Built-in acoustic piano harmonic physical modeling engine running on low-latency `AudioTrack` (44.1 kHz, 16-bit PCM stereo).
- Produces realistic grand piano sound with hammer impulse attacks and sustain resonance with zero external soundfont plugins required.
- External audio file synchronization (WAV, MP3, AAC) along the multi-track timeline.

### 9. ✏️ Interactive Piano Roll MIDI Editor
- Visual piano roll grid with note dragging, pitch repositioning, duration resizing, deletion, and insertion.
- Grid quantization ($1/4$, $1/8$, $1/16$, $1/32$ notes).
- Full Undo / Redo history stack.

### 10. 🌌 10 Built-in Atmospheric Presets
1. `Moonlight`: Signature deep indigo night with glowing cyan beams.
2. `Lunar Glass`: Frosted refractive glass with ethereal reflections.
3. `Aurora`: Luminescent emerald and teal polar light curtains.
4. `Cyber Piano`: Futuristic synthwave with electric magenta pulses.
5. `Crystal Gem`: Prismatic gemstones with specular sparkles.
6. `Cosmic Stardust`: Deep nebula void with supernova particle bursts.
7. `Minimal Studio`: Clean monochrome design for utmost focus.
8. `Cinematic Gold`: Warm anamorphic embers and cinematic depth.
9. `Electric Arc`: High-voltage lightning crackling on chords.
10. `Dream Cloud`: Soft pastel watercolor waves in celestial twilight.

### 11. 🧠 AI-Assisted Style Advisor & Visual Scripting
- **AI Advisor**: Automatically inspects MIDI tempo (BPM), note density, pitch range, and dynamic velocity to suggest the best matching visual preset.
- **Visual Scripting**: Rule-based event automations (e.g., *If velocity > 100 → boost particles*, *If pitch > 84 → burst cosmic stardust*, *If chord size ≥ 4 → dynamic camera tilt*).

### 12. 🔒 100% Free, Offline & Privacy-First
- **No subscriptions or in-app purchases**.
- **No mandatory account creation or cloud sync**.
- **No export watermarks**.
- All MIDI files, audio recordings, and videos stay strictly on your local device.

---

## 📂 Project Structure

```
Lyane/
├── app/
│   ├── build.gradle.kts                      # App Gradle module configuration
│   ├── proguard-rules.pro                   # R8 / Proguard keep rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml          # Permissions, activities, MIDI filters
│       │   ├── assets/
│       │   │   ├── demo_midi/               # Beethoven, Debussy, Bach demo MIDIs
│       │   │   ├── demo_xml/                # Clair de Lune MusicXML demo
│       │   │   ├── presets/                 # 10 atmospheric preset JSON files
│       │   │   └── shaders/                 # Vertex & fragment GLSL shaders
│       │   ├── java/com/lyane/app/
│       │   │   ├── LyaneApplication.kt      # Application initialization
│       │   │   ├── core/
│       │   │   │   ├── ai/                  # AI Style Suggester
│       │   │   │   ├── audio/               # LyaneSynthesizer & AudioPlayerSync
│       │   │   │   ├── camera/              # Camera3DController (Perspective matrix)
│       │   │   │   ├── editor/              # MidiEditorEngine (Undo/Redo, Quantize)
│       │   │   │   ├── export/              # VideoExporter (MediaCodec offline MP4)
│       │   │   │   ├── lighting/            # Reactive lighting & bloom
│       │   │   │   ├── midi/                # MidiParser, Serializer, PlaybackEngine, AndroidMidiManager
│       │   │   │   ├── musicxml/            # MusicXmlParser
│       │   │   │   ├── particles/           # ParticleEngine (Physics & bursts)
│       │   │   │   ├── presets/             # PresetManager
│       │   │   │   ├── scripting/           # VisualScriptingEngine
│       │   │   │   └── visual/              # VisualEngine, FallingNotesRenderer, KeyboardRenderer
│       │   │   ├── data/
│       │   │   │   ├── model/               # MidiNote, MidiSequence, VisualConfig, ExportConfig
│       │   │   │   └── repository/          # ProjectRepository
│       │   │   └── ui/
│       │   │       ├── MainActivity.kt      # Single-Activity Navigation Host
│       │   │       ├── components/          # Logo, VisualizerView, PianoRoll, TimelineBar, PresetCard
│       │   │       ├── screens/             # Home, Studio, Editor, Presets, Timeline, Export, Settings, Onboarding
│       │   │       ├── theme/               # Material3 Lyane Lunar Theme & Typography
│       │   │       └── viewmodel/           # MainViewModel
│       │   └── res/
│       │       ├── drawable/                # ic_lyane_logo, splash backgrounds
│       │       ├── mipmap-*/                # High-res application icons (mdpi-xxxhdpi)
│       │       ├── values/                  # strings.xml, colors.xml, themes.xml
│       │       └── xml/                     # backup_rules.xml, data_extraction_rules.xml
│       └── test/java/com/lyane/app/
│           └── LyaneUnitTest.kt             # Full unit test suite
├── gradle/
│   ├── libs.versions.toml                   # Version catalog dependencies
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── web/                                     # Live Interactive Web Studio Preview
│   ├── index.html                           # Full WebAudio / Canvas Studio App
│   └── server.js                            # Node static server (port 3000)
├── .github/workflows/build-apk.yml          # CI: real Gradle build -> signed APK -> GitHub Release
├── build_apk.py                             # (deprecated) legacy placeholder packager, do not use
├── run_tests.py                             # Verification test suite
├── build.gradle.kts                         # Root Gradle build script
├── settings.gradle.kts                      # Gradle settings
├── gradlew                                  # Gradle wrapper executable
└── README.md                                # Comprehensive documentation
```

---

## 🛠️ Build & Installation

### Option 1: Direct APK Installation
Download the latest signed release APK from the [GitHub Releases page](https://github.com/Aseptiosmic/Lyane/releases/latest), then install it:
```bash
# Install via ADB on any connected Android device or emulator
adb install -r Lyane.apk
```
> The APK is built automatically by the `Build and Release Real APK` GitHub Actions workflow from the source in this repository, so it always reflects the latest compiled code (not a placeholder file).

### Option 2: Build with Android Studio / Gradle
1. Clone the repository into Android Studio:
   ```bash
   git clone https://github.com/Aseptiosmic/Lyane.git
   ```
2. Open the project in **Android Studio Hedgehog / Iguana / Jellyfish (JDK 17+)**.
3. Sync Gradle and build the Release APK:
   ```bash
   ./gradlew assembleRelease
   ```
4. The output APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

---

## 🧪 Testing & Verification

Run the test suite to verify MIDI parsing, MusicXML importing, piano roll editing, 3D perspective projection, and APK archive validity:
```bash
python3 run_tests.py
```

All 35 verification checks will run and pass:
- ✅ MIDI Parser / Serializer roundtrip integrity
- ✅ MusicXML Score to MIDI sequence conversion
- ✅ Multi-track timeline & quantization engine
- ✅ 3D perspective coordinate math
- ✅ 88-key piano pitch mapping
- ✅ 10 Visual preset configurations
- ✅ Signed APK package structure verification

---

## 🌐 Live Web Studio Preview

Lyane includes a live browser companion on port `3000` implementing the same falling notes engine, 3D camera controls, interactive 88-key piano, WebAudio synthesizer, and offline MP4 exporter:
```bash
node web/server.js
# Access via http://localhost:3000
```

---

## 📜 License & Privacy

Lyane is 100% Free and Open Source. Created for pianists, educators, creators, and music enthusiasts worldwide.
*No tracking. No accounts. No subscriptions. No watermarks.*
