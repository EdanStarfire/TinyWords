This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Last updated: 2025-07-10

Build, Lint, and Test

- Build app:./gradlew assembleDebug
- Run app (Android Studio):Open in Android Studio and run the app on an emulator or device.
- Run all tests:./gradlew testDebugUnitTest(For instrumented tests: ./gradlew connectedDebugAndroidTest)
- Lint:./gradlew lint

Project Structure & Architecture

- App: Android (Kotlin, Jetpack Compose, Hilt, DataStore, ViewModel/StateFlow, TTS)
- Core Logic:
    - GameViewModel: Holds all central game state, logic, and TTS orchestration.
    - WordChallengeGenerator and related data classes handle word/challenge setup.
    - TTS handled by TtsHelper and triggered by user/game events.
- Assets:
    - Word list/definitions: assets/word_definitions.json
    - Placeholder images: res/drawable/placeholder_*.png
- UI:
    - Compose-based; main UI flows via MainActivity.
    - Core flows: target word, image choices, feedback via TTS & UI, borders/buttons for utility.

Development Plan

- Feature/logic development follows checklists in PRD_DevelopmentPlan.md.
- See PRD_GameUX.md for target experience/user interaction details.

Notes

- Uses Gradle Version Catalog for managing dependencies.
- minSdk: 34, targetSdk: 36, JDK 21.
- Instrumented and unit tests should be runnable via Gradle commands above.
