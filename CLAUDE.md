This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Last updated: 2025-07-17

Purpose & Context

TinyWords is an educational Android game focused on phonics, word recognition, and image association. Core gameplay presents “word challenges” with images and TTS feedback, using Compose UI and centralized game logic.

Build, Lint, and Test

- Build app: ./gradlew assembleDebug
- Run app (Android Studio): Open in Android Studio and run the app on an emulator or device.
- Run all unit tests: ./gradlew testDebugUnitTest
- Run all instrumented tests: ./gradlew connectedDebugAndroidTest (requires emulator or device)
- Lint: ./gradlew lint

Project Structure & Architecture

- App: Android (Kotlin, Jetpack Compose, Hilt for DI, DataStore, ViewModel/StateFlow, TTS)
- Core Logic:
    - GameViewModel: Centralizes game state, event logic, and TTS orchestration. All game updates and UI flows should be routed via the ViewModel. UI must only observe StateFlow and must not trigger logic directly.
    - WordChallengeGenerator: Handles word/challenge construction; supports deterministic/test mode for repeatable tests.
    - TtsHelper (under tts/): Provides all Text-to-Speech capabilities. Always use TtsHelper for TTS logic; do not interact directly with TextToSpeech APIs.
- Assets:
    - Word list/definitions: app/src/main/assets/word_definitions.json
    - Placeholder and word images: app/src/main/res/drawable/placeholder_1.png, placeholder_2.png, placeholder_3.png and CVC image set (follow naming convention *_image.png for all word images)
- UI:
    - Jetpack Compose-based; entrypoint in MainActivity sets all main content.
    - UI components must remain logic-free, observing only the ViewModel state. All user actions must trigger events on the ViewModel.
- Dependency Versions:
    - All dependencies MUST be added or updated via gradle/libs.versions.toml. Do NOT add new library versions directly in build.gradle.kts files.
- Settings:
    - Persisted with Jetpack DataStore; toggling settings (e.g., pronounce target at start, auto-advance, TTS speed) updates game logic flows immediately. Update settings only through recommended ViewModel methods.

TTS & Target Word Rules

- At challenge start: Only pronounce the target word if the "Pronounce Target Word at Start" setting is enabled (see ViewModel and Compose logic for correct implementation).
- Do NOT call TTS for the target word automatically from Compose unless the setting is ON.
- All hint, feedback, and manual pronunciations must flow through ViewModel event methods.
- All TTS calls must go through TtsHelper via the ViewModel—never use TextToSpeech APIs directly or in Compose.

Best Practices for Agents and Contributors

- Use Hilt for Dependency Injection throughout. Do NOT use manual dependency wiring or service locators.
- When adding/changing words or images, use consistent naming, and update all references and tests (see WordDefinitionTest for required image coverage).
- Always extend code by following the Model-View-ViewModel(Device)-DataStore-testable pattern; never introduce UI logic or resource handling outside of these flows.
- For all new logic, provide both unit and (where appropriate) instrumented tests. Tests that require stable challenge data MUST use deterministic generator mode. See PRD_DevelopmentPlan.md for test strategy.

Automated Agent and LLM-specific Rules

- Do NOT run or suggest automatic lint, build, debugging, or test commands—these actions should only be run if explicitly requested by the user. Claude Code is often developing for an external/linked build environment.
- Do NOT add documentation files other than this one unless directly requested. Always summarize new architecture or resource patterns in CLAUDE.md.
- If you make changes to game logic, architecture, resource handling, or persistent patterns, update this file with a concise summary of new conventions and rationale.

Getting Oriented / Quick Reference

- Review GameViewModel for game state and orchestration; WordChallengeGenerator for challenge/resource creation; and TtsHelper for TTS access.
- PRD_DevelopmentPlan.md and PRD_GameUX.md define logic, sequence, and UX requirements—always align features to these docs.
- When handling TTS, settings, asset, or gameplay changes, follow the established MVVM/DataStore/testable pattern.

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
