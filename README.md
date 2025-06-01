
# Dune - Jellyfin Android TV Client

[![License: GPL v2](https://img.shields.io/badge/License-GPL_v2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

Copyright (C) 2025 Sam42a  
Based on Jellyfin Android TV (C) Jellyfin Contributors

<p align="center">
  <img src="https://i.imgur.com/qc4bNya.png](https://imgur.com/qc4bNya)?sanitize=true" alt=width="400">
</p>




## About

**Dune** is a modified version of the official [Jellyfin](https://jellyfin.org/) Android TV client with UI/UX improvements and customizations.

> **Note**: This is an unofficial fork/repository not affiliated with the Jellyfin project. The official Jellyfin Android TV client can be found at [jellyfin/jellyfin-androidtv](https://github.com/jellyfin/jellyfin-androidtv).

## Modifications

This fork includes the following changes from the official client:

### UI/UX Improvements
- **Homescreen Overhaul**: redesigned homescreen with improved layout and visual hierarchy
- **Login Screen Redesign**: Enhanced login experience with modern design elements
- **Search Screen**: Complete redesign with dedicated voice-to-text icon for easier navigation
- **Dark Theme**: Improved dark theme based on [jellyfin-androidtv-OLED](https://github.com/LitCastVlog/jellyfin-androidtv-OLED)
- **Visual Cleanup**: Optimized animations and visual elements for better performance

### Customization Options
- **Library Views**:
  - Display library folders as sleek, simple buttons
  - Option to show primary images as backdrops for library folders
- **Homescreen Rows**:
  - Add genre-based rows to the homescreen
  - Add personal favorites/collections rows
  - Show/hide specific rows based on user preference
- **Card Sizes**:
  - Improved card sizes for better visibility across different screen sizes
  - Movies in "Continue Watching" now match episode card sizes
  - Movie thumb images in "Continue Watching" for a consistent look

### Media Playback
- **Subtitles**: Enhanced subtitle customization options
- **Backdrops & Backgrounds**: Customizable backdrop and background effects

### Technical Improvements
- Performance optimizations throughout the app
- Modified app name and package for side-by-side installation with official client


## Acknowledgments

This project is based on the work of the Jellyfin Contributors. Special thanks to all the developers and community members who have contributed to the Jellyfin Android TV project.

## License

This project is licensed under the **GNU General Public License v2.0 (GPL-2.0)**. See the [LICENSE](LICENSE) file for details.

```

## Third-Party Libraries

This project uses the following third-party libraries:

- **Jellyfin SDK** - [GPL-2.0](https://github.com/jellyfin/sdk-kotlin)
- **AndroidX Libraries** - [Apache-2.0](https://developer.android.com/jetpack/androidx)
- **Kotlin Coroutines** - [Apache-2.0](https://github.com/Kotlin/kotlinx.coroutines)
- **Koin** - [Apache-2.0](https://insert-koin.io/)
- **Coil** - [Apache-2.0](https://coil-kt.github.io/coil/)
- **Markwon** - [Apache-2.0](https://noties.io/Markwon/)
- **Timber** - [Apache-2.0](https://github.com/JakeWharton/timber)
- **ACRA** - [Apache-2.0](https://github.com/ACRA/acra)
- **Kotest** - [Apache-2.0](https://kotest.io/)
- **MockK** - [Apache-2.0](https://mockk.io/)


```

## Contributing

Contributions are welcome! Please open an issue first to discuss what you would like to change.

## Source Code

This project is a modified version of the Jellyfin Android TV client. The complete source code, including modifications, is available under the terms of the GPL-2.0 license.

For developers working with modified files, please include the following header at the top of any significantly modified files:

```
/*
 * This file is part of Dune, a modified version of Jellyfin Android TV.
 * Original source: [original file path in Jellyfin repo]
 * Modifications (C) 2025 Sam42a
 */
```

## Disclaimer

This is an unofficial fork of the Jellyfin Android TV client. The Jellyfin name and logo are registered trademarks of the Jellyfin Project. This project is not affiliated with or endorsed by the Jellyfin Project.

## Translating

This project uses the same translation system as the original Jellyfin Android TV client. If you'd like to help with translations, please contribute to the [official Jellyfin Weblate instance](https://translate.jellyfin.org/projects/jellyfin-android/jellyfin-androidtv).

## Build Process

### Dependencies

- Android Studio

### Build

1. Clone or download this repository

   ```sh
   git clone https://github.com/Sam42a/jellyfin-androidtv-Enhanced.git
   cd jellyfin-androidtv-Enhanced
   ```

2. Open the project in Android Studio and run it from there or build an APK directly through Gradle:

   **Standard Version:**
   ```sh
   ./gradlew assembleStandardDebug  # Debug build
   ./gradlew assembleStandardRelease  # Release build
   ```
   
   **Enhanced Version (installable alongside original Jellyfin):**
   ```sh
   ./gradlew buildEnhanced  # Custom task to build enhanced release version
   ```
   
   Add the Android SDK to your PATH environment variable or create the ANDROID_SDK_ROOT variable for
   this to work.

### Deploy to device/emulator

   ```sh
   # Install standard version
   ./gradlew installStandardDebug
   
   # Install enhanced version (can coexist with original Jellyfin)
   ./gradlew installEnhancedRelease
   ```

**Note:** The enhanced version uses package ID `org.jellyfin.androidtv.enhanced` which allows it to be installed alongside the original Jellyfin app.

### Build System Requirements

- Java 21 or higher
- Android SDK with API level 35
