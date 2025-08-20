This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Last updated: 2025-07-24

Purpose & Context

TinyWords is an educational Android game focused on phonics, word recognition, and image association. Core gameplay presents “word challenges” with images and TTS feedback, using Compose UI and centralized game logic.

Quick Navigation & File References

- Use this section to quickly find relevant files, folders, and code entrypoints:
    - App entry: app/src/main/java/com/edanstarfire/tinywords/MainActivity.kt
    - Core game logic: app/src/main/java/com/edanstarfire/tinywords/ui/game/GameViewModel.kt
    - Challenge generation: app/src/main/java/com/edanstarfire/tinywords/WordChallengeGenerator.kt
    - TTS helper/util: app/src/main/java/com/edanstarfire/tinywords/tts/TtsHelper.kt
    - Asset JSON: app/src/main/assets/word_definitions.json
    - All word/placeholder images: app/src/main/res/drawable/ (use *image.png, placeholder_*.png)
    - Settings, game/data persistence: app/src/main/java/com/edanstarfire/tinywords/GameSettingsPreferences.kt, ScoreStreakPreferences.kt
    - UI theme/colors/typography: app/src/main/java/com/edanstarfire/tinywords/ui/theme/
    - Unit tests: app/src/test/java/ (mirrors logic locations)
    - Instrumented (device/UI) tests: app/src/androidTest/java/
    - Dependencies: gradle/libs.versions.toml (always add/update here)

Folder Structure & Orientation

- app/src/main/java/com/edanstarfire/tinywords/: All main app classes
    - tts/: Text-to-Speech helpers (use TtsHelper, don’t call TTS APIs directly)
    - ui/: Compose UI, separated by feature (game/, theme/, etc.)
- app/src/main/res/drawable/: Images/assets; all images must conform to *image.png naming and be referenced in asset JSON/tests
- app/src/test/java/ and .../androidTest/java/: Mirror main folder logic for tests

Build, Lint, and Test (run only if requested)

**Windows (uses gradlew.bat with auto-configured JAVA_HOME):**
- Build app: cmd.exe //c "gradlew.bat assembleDebug"
- Run unit tests: cmd.exe //c "gradlew.bat testDebugUnitTest"
- Run instrumented tests: cmd.exe //c "gradlew.bat connectedDebugAndroidTest" (requires emulator/device)
- Lint: cmd.exe //c "gradlew.bat lint"

**Linux/Mac:**
- Build app: ./gradlew assembleDebug
- Run unit tests: ./gradlew testDebugUnitTest
- Run instrumented tests: ./gradlew connectedDebugAndroidTest (requires emulator/device)
- Lint: ./gradlew lint

Coding Guidelines & Structure

- Always use Jetpack Compose for all UI. Never introduce Activity/Fragment XML layouts.
- UI code: Must not contain logic. Compose components strictly observe ViewModel StateFlow, and trigger events (never direct logic calls).
- Logic, TTS, progression, and persistence: All flows through GameViewModel, WordChallengeGenerator, or helpers. Never wire dependencies manually—use Hilt DI for all non-static dependencies.
- Settings: Only modified via ViewModel methods. Backed by Jetpack DataStore, instant/live update to all state as settings change.
- TTS: All TTS API calls go through TtsHelper; trigger only from ViewModel (never directly/composable). Do not TTS the target word at challenge start unless setting is enabled.
- Assets: Always name images *image.png. Update references in JSON and tests (see WordDefinitionTest for coverage requirements).
- Tests: For all new logic, add both unit (core logic, deterministic) and instrumented (UI/flows) where possible. Use deterministic generator mode for stable test data.
- Dependencies: Only add/change via gradle/libs.versions.toml. Never direct in build.gradle.kts.

Getting Oriented Quickly

- Review GameViewModel for central game state/events.
- WordChallengeGenerator for word logic, construction, and generator settings.
- TtsHelper for all TTS logic.
- Look to PRD_DevelopmentPlan.md and PRD_GameUX.md for complete task, UX, and logic sequence requirements. 
- Check assets/, res/drawable/, and word_definitions.json for word/image/setting/asset references and expected conventions.

Gotchas & Coding Conventions

- Always use the Model-View-ViewModel(DataStore)-testable pattern. Never place logic or state in Compose/Activity/Fragment.
- Hilt DI is mandatory for injectable logic/helpers. No service locators or manual DI.
- All state that drives UI must live in the ViewModel and be exposed as StateFlow. Do not trigger navigation or UI changes directly from Compose.
- Never manipulate settings, resources, or TTS directly from UI; always go through ViewModel and helpers.
- Resource/image/test references must be updated together—image changes require test updates; test refs require asset coverage.
- Commit messages: 1-2 sentence summary, details below blank line, bot/assistant tag as below, reference checklist/PRD task where possible.

Reference Table (files/locations)

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
