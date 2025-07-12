This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Last updated: 2025-07-12

Build, Lint, and Test

- Build app: ./gradlew assembleDebug
- Run app (Android Studio): Open in Android Studio and run the app on an emulator or device.
- Run all unit tests: ./gradlew testDebugUnitTest
- Run all instrumented tests: ./gradlew connectedDebugAndroidTest (requires emulator or device)
- Lint: ./gradlew lint

Project Structure & Architecture

- App: Android (Kotlin, Jetpack Compose, Hilt for DI, DataStore, ViewModel/StateFlow, TTS)
- Core Logic:
    - GameViewModel: Centralizes game state, event logic, and TTS orchestration. All game updates and UI flows should be routed via the ViewModel.
    - WordChallengeGenerator: Handles word/challenge construction; supports deterministic/test mode for repeatable tests.
    - TtsHelper (under tts/): Provides all Text-to-Speech capabilities. Always use TtsHelper for TTS logic; do not interact directly with TextToSpeech APIs.
- Assets:
    - Word list/definitions: app/src/main/assets/word_definitions.json
    - Placeholder images: app/src/main/res/drawable/placeholder_1.png, placeholder_2.png, placeholder_3.png (follow naming placeholder_*.png for all word images)
- UI:
    - Jetpack Compose-based; entrypoint in MainActivity sets all main content.
    - UI components must remain logic-free, observing only the ViewModel state. All user actions must trigger events on the ViewModel.
- Dependency Versions:
    - All dependencies MUST be added or updated via gradle/libs.versions.toml. Do NOT add new library versions directly in build.gradle.kts files.

Development Process

- Follow the detailed, evolving checklist in PRD_DevelopmentPlan.md when developing or adding features—always refer for correct task/state sequence.
- Adhere to the experience and UI guidelines in PRD_GameUX.md.
- Use Hilt for Dependency Injection throughout. Do NOT use manual dependency wiring or service locators.
- When adding/changing words, images, or resources, use consistent naming, and update all references/tests as needed.
- If you add new TTS-dependent functionality, ensure TTS can gracefully degrade (no crash) and is mockable/disableable in tests.
- When updating core logic (GameViewModel or WordChallengeGenerator), ensure all relevant tests pass in both unit and instrumented environments. Use the deterministic WordChallengeGenerator test mode.

Testing

- Use only Gradle commands above to execute all tests. Do not rely solely on IDE features for testing.
- For all new logic, provide both unit and (where appropriate) instrumented tests. Tests that require stable challenge data MUST use deterministic generator mode.
- Instrumented tests (connectedDebugAndroidTest) require an emulator/device attached and should be run in repeatable environments.

Version Control, Commit, and PR Conventions

- Use git for all version control.
- Propose commits for each significant or atomic change. Practice small, focused commits.
- Commit message:
    - First line: Simple, clear statement (max 1-2 sentences).
    - Detailed explanation as needed below, separated by a blank line.
    - Bot or assistant commits: add "🤖 Generated with [Claude Code](https://claude.ai/code)" and co-author tag as seen in previous auto-generated commits.
- Open PRs following established commit messaging. Add references to features/issues addressed and checklist item numbers.
- Do NOT add documentation files other than this one unless directly requested.

Reference Table (file locations)

| Area              | Path/Pattern                                                            |
|-------------------|------------------------------------------------------------------------|
| App entrypoint    | app/src/main/java/com/edanstarfire/tinywords/MainActivity.kt            |
| Game logic        | app/src/main/java/com/edanstarfire/tinywords/ui/game/GameViewModel.kt   |
| Challenge logic   | app/src/main/java/com/edanstarfire/tinywords/WordChallengeGenerator.kt  |
| TTS helper        | app/src/main/java/com/edanstarfire/tinywords/tts/TtsHelper.kt           |
| Assets            | app/src/main/assets/word_definitions.json                               |
| Images            | app/src/main/res/drawable/placeholder_*.png                            |
| Dependencies      | gradle/libs.versions.toml                                               |
| Build config      | app/build.gradle.kts                                                    |
| Unit tests        | app/src/test/java/                                                      |
| Instrumented tests| app/src/androidTest/java/                                               |

If contributing:
- Document any new patterns or changes to conventions directly in this file during your PR.
- For nonstandard decisions (e.g., minSdk/targetSdk, JDK 21 choice), add rationale in commit or PR notes if you introduce a further change.
