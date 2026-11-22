# FocusGuard — Native Offline Screen-Time & Distraction Blocker

FocusGuard is a complete, production-grade native Android app built with **100% Jetpack Compose and Material 3**. Designed in the spirit of apps like Regain, ScreenZen, and Decafly, FocusGuard empowers users to take back control of their attention and eliminate doomscrolling habits.

**Key Guarantee**: **100% Local and Offline**. No login, no cloud sync, no tracking, and **no `android.permission.INTERNET` requested**. Everything is stored on-device in Room (SQLite) and DataStore Preferences.

---

## 📱 Feature Architecture

### 1. Onboarding Flow (First Launch Only)
- **Screen 1**: *"Take back control of your time"* — soft gradient backdrop with shield illustration.
- **Screen 2**: *"Block distracting apps & short-form content"* — illustrates targeted feed blocking.
- **Screen 3**: *"FocusGuard needs 2 permissions to work"*:
  - **Usage Access**: Polled live via `AppOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS)` with auto-updating green checkmark.
  - **Accessibility Service**: Checked live via `AccessibilityManager.getEnabledAccessibilityServiceList()`.
  - Privacy note emphasizing local data isolation.

### 2. Home / Dashboard Screen
- **Greeting Header**: Context-aware greeting ("Good morning / afternoon / evening") with today's date.
- **Circular Progress Ring**: Watch-face style circular gauge with animated sweep and dynamic color shifts (Green `< 70%` → Amber `70–92%` → Red `> 92%`).
- **Quick Actions Row**: 1-tap chips to *Start Focus*, *Block Now*, or *View Report*.
- **Most Used Apps List**: App icon, name, relative usage bar against the top app, and lock indicators.
- **Smart Insight Card**: Locally computed delta comparing today's usage vs yesterday's (e.g. *"You've spent 1h 20m more on Instagram than yesterday"*).

### 3. Block Tab (Core Distraction Shield)
- **Full App Blocker**:
  - Filterable list of all installed apps with system app toggle and search.
  - Interactive bottom sheet to set daily limits (`15m`, `30m`, `1h`, `2h`, custom) or block completely.
- **Smart Short-Form Blocker**:
  - Intercepts **YouTube Shorts**, **Instagram Reels**, **Instagram Explore**, **Facebook Reels**, **Snapchat Spotlight**, and **TikTok FYP**.
  - Traverses the `AccessibilityNodeInfo` tree to match resource ID fragments and content descriptions.
  - Kicks the user out via `GLOBAL_ACTION_BACK` and surfaces a friction overlay with a mandatory **3-second cooldown** (*"Breathe... Is this intentional?"*).
  - Backed by the local `short_content_rules` Room table for easy updates when third-party apps change view IDs.
- **Website Blocker**: Real-time URL bar detection for browser packages.
- **Strict Mode (PRO)**: Intercepts attempts to stop sessions early or tamper with accessibility settings via `DeviceAdminReceiver`.

### 4. Focus Tab
- **Pomodoro Focus Timer (PRO)**: Customizable work & break intervals with circular countdown ring.
- **Stopwatch Mode (Free)**: Clean count-up focus timer.
- **Procedural Ambient Sound Generator (PRO)**:
  - Generates **White Noise**, **Gentle Rain**, and **Binaural Beats (40Hz Gamma)** procedurally in real-time using `AudioTrack` PCM synthesis.
  - **Zero file downloads, zero megabytes, seamless infinite loops, 100% offline**.
- **Focus Themes**: Emerald Calm, Slate Minimal, Lofi Sunset, Deep Forest, Midnight Cyber.
- **Streaks**: Automatic consecutive daily streak tracking with flame badge.

### 5. Stats Tab
- **Canvas-Drawn Charts**: Daily Bar Chart, Weekly Bézier Trend Line Chart, and Top-5 App Donut Chart.
- **Time Saved Metric**: Real-time arithmetic of minutes reclaimed based on blocked distraction attempts.
- **CSV Data Export**: Directly writes formatted `.csv` reports to device `Downloads/` via MediaStore.

### 6. Settings & Premium
- Daily screen time goal slider (1h to 10h).
- Local continuous screen-time break reminders.
- Permanent whitelist (Phone, Emergency SOS, Settings).
- Glance home-screen app widget with 1-tap action.
- Local `isPremium` toggle supporting feature gating.

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (BOM 2024.11.00) + Material 3
- **Architecture**: MVVM with ViewModel, StateFlow, and Repository Pattern
- **Local Persistence**: Room 2.6.1 + DataStore Preferences 1.1.1
- **Background Tasks**: WorkManager 2.10.0
- **App Widgets**: Jetpack Glance 1.1.1
- **Target SDK**: 35 (Android 15) | **Min SDK**: 26 (Android 8.0)

---

## 🚀 Building & Running

### Option A: Via Android Studio
1. Open Android Studio.
2. Select **Open an Existing Project** and choose this directory (`FocusGuard`).
3. Allow Gradle to sync.
4. Run on a connected physical device or emulator.

### Option B: Via Command Line (Gradle)
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

To install directly to a connected Android device via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
