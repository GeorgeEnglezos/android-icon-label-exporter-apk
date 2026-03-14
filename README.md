# Icon Helper APK

A minimal Android application that extracts app icons and labels from an Android device, exported for use by the Scrcpy GUI desktop app.

## Features

- Single-button export of all installed app icons and labels
- PackageManager-based extraction (fast, reliable)
- Outputs standard PNG icons and JSON labels file
- Minimal size, no heavy dependencies

## Building

### Prerequisites

- Android SDK 26+ (API Level 26 or higher)
- Kotlin 1.9+
- Gradle 8.1+

### Build Steps

```bash
./gradlew build
```

**Output:** `build/outputs/apk/release/app-release.apk`

### Install on Device

```bash
adb install build/outputs/apk/release/app-release.apk
```

## Usage

1. Open the app on your Android device
2. Tap "Export Icons"
3. Wait for progress indicator to complete
4. Icons and labels are exported to device cache
5. Desktop Scrcpy GUI app automatically pulls and caches them

## Export Format

**Location on device:** `/sdcard/Android/data/com.george.iconhelper/files/iconhelper/`

**Files:**
- `labels.json` — JSON map of `packageName → humanReadableName`
- `categories.json` — JSON map of `packageName → categoryName`
- `icons/` — Directory containing `{packageName}.png` files (max 192×192 px)

**Example labels.json:**
```json
{
  "com.example.app": "Example App",
  "com.android.chrome": "Chrome",
  "com.whatsapp": "WhatsApp"
}
```

## Architecture

### Main Components

- **MainActivity.kt** — Single activity with export button and progress UI
- **IconExtractor.kt** — Converts app icons from PackageManager to PNG files
- **LabelExtractor.kt** — Retrieves human-readable app names
- **ExportWriter.kt** — Manages file I/O for icons and labels.json

### Permissions

- `QUERY_ALL_PACKAGES` — Required to enumerate all installed apps on Android 11+
- `WRITE_EXTERNAL_STORAGE` — Required on Android 6–9 only; not needed on Android 10+

## Development

### Running Unit Tests

```bash
./gradlew test
```

### Installing Debug APK

```bash
./gradlew installDebug
```

### Debugging

View device logs during export:

```bash
adb logcat com.george.iconhelper:V
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Permission denied" | Ensure QUERY_ALL_PACKAGES is granted (request at runtime on Android 11+) |
| App crashes during export | Check logcat: `adb logcat com.george.iconhelper` |
| No icons exported | Some system apps may not have extractable icons; app gracefully skips them |
| APK won't install | Verify device is connected: `adb devices` |

## Integration with Scrcpy GUI

This APK is a companion to [Scrcpy GUI](https://github.com/GeorgeEnglezos/Scrcpy-GUI), a desktop app for controlling Android devices.

## License

[GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE) — same license as [Scrcpy GUI](https://github.com/GeorgeEnglezos/Scrcpy-GUI).
