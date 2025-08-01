# TinyWords - MVP Development Plan

This document outlines the development plan for the Minimum Viable Product (MVP) of the TinyWords game, based on the specifications in `PRD_GameUX.md`.

## Phase 1: Project Setup & Core Structure

*   **Task 1.1: New Project Creation**
    *   [x] Set up a new Android Studio project.
    *   [x] Configure `build.gradle` with necessary dependencies (e.g., Kotlin Coroutines, Jetpack Compose, ViewModel, LiveData/StateFlow, Jetpack DataStore).
*   **Task 1.2: Basic Asset & Initial TTS Management**
    *   [x] Create placeholder images for the three choices (`placeholder_1.png`, `placeholder_2.png`, `placeholder_3.png` added to `res/drawable`).
    *   [x] Implement a basic Text-To-Speech (TTS) helper class for word pronunciations (target words, "Correct!", "Try Again").
        *   [x] Handle TTS engine initialization and lifecycle.
    *   [x] Set up `strings.xml` for all user-facing text (initial target words, button labels, messages, TTS phrases like "Correct", "Try Again").
*   **Task 1.3: Data Structures for Words**
    *   [x] Define how the word list will be stored (e.g., hardcoded list, JSON in assets).
    *   [x] Define data class for a "Game Round" or "Word Challenge" (e.g., `WordChallenge(targetWord: String, correctImageWord: String, incorrectImageWord1: String, incorrectImageWord2: String, targetImageRes: Int, incorrect1ImageRes: Int, incorrect2ImageRes: Int)`).
*   **Task 1.4: Core Game Logic - State Management**
    *   [x] Create `GameViewModel` (extending `androidx.lifecycle.ViewModel`).
    *   [x] Inject or provide access to the TTS helper in `GameViewModel`.
    *   [x] Define state holders in `GameViewModel`:
        *   Current target word.
        *   Current image choices (associated words, drawable IDs).
        *   Correct/incorrect status.
        *   Score/streak.
        *   Hint status.
        *   Timer state.
        *   Settings values.
    *   [x] Implement basic functions in `GameViewModel` (logic for all core flows—hint, reset, choice, new game, settings update, and auto-advance—all present and functional):
        *   [x] `loadNewWordChallenge()` (pronounce new target word)
        *   [x] `processPlayerChoice(selectedWord: String)` (pronounce result/feedback)
        *   [x] `requestHint()`
        *   [x] 2025-07-25: Enhanced Tier 1 to spell word by TTS instead of pronouncing full word.
        *   [x] `resetGame()`
        *   [x] `startAutoAdvanceTimer()` / `cancelAutoAdvanceTimer()`
        *   [x] `updateSettings(newSettings: GameSettings)`
    *   [x] Update WordChallengeGenerator to disable randomness for testing purposes.
    *   [x] **2025-07-25: Bugfix – Auto-Advance interval is preserved on toggle:**
        * Fix: Toggling auto-advance OFF does not clear the stored delay value. Toggling ON restores the previous delay, always enforcing minimum value. This is managed in GameViewModel and GameSettingsPreferences, not in UI state. Tested live and via instrumented test.


## Phase 2: UI Implementation - Main Game Screen (Portrait First with Jetpack Compose)

*   **Task 2.1: Basic Layout (Portrait)**
    *   [x] Create main activity & Jetpack Compose content view.
    *   [x] Implement portrait layout: `TargetWordArea`, `ImageChoicesArea`, `GameBorder`, internal dividers.
*   **Task 2.2: Target Word Display & Pronunciation**
    *   [x] Composable for target word from `GameViewModel`.
    *   [x] Trigger TTS pronunciation of the target word via `GameViewModel` when it appears/changes.
    *   [x] Implement dynamic highlighting for Hint Tier 1, matching PRD logic (highlight the differing letter position among image choices for the current target word).
    *   [x] **2025-07-25: Bugfix – Shrink target word font (portrait):**
        * Target word font size reduced slightly in portrait mode to prevent bottom clipping; now displays cleanly without cutoff across devices."
*   **Task 2.3: Image Choices Display & Interaction**
    *   [x] Reusable Composable for a single image choice.
    *   [x] Display three images from `GameViewModel`.
    *   [x] Implement tap handling, linking to `GameViewModel.processPlayerChoice()`.
    *   [x] Grey out and disable any image choice immediately after an incorrect tap as per PRD feedback flow.
*   **Task 2.4: Feedback Implementation (Visual & TTS Auditory)**
    *   [x] Visual feedback: dynamic border for correct/incorrect, visual error persists for all wrong choices (animation pending).
    *   [x] Auditory feedback: TTS for "Correct!" / "Try Again" / selected word replay via `GameViewModel`.
    *   [x] After each incorrect selection, visually grey out (desaturate) only the chosen image and disable its tap, as per GameUX feedback.
    *   [x] Show words under images after correct or incorrect answers, with only the differing letter highlighted (green for correct, red for incorrect), following the feedback sequence in the PRD (animation or more detail pending).
*   **Task 2.5: Border UI Elements (Static Display & Feedback Logic)**
    *   [x] Composables for `ScoreStreakDisplay`, `RestartButton`, `HelpButton`, `OptionsButton`.
    *   [x] Position within `GameBorder`. Display initial values from `GameViewModel`.
    *   [x] Implement logic for "I Need Help" button: enabled at the start of each new word, disabled after using Tier 2, re-enabled for the next word, matching UX rules.
    *   [x] Ensure feedback animations (shake, star burst, border color) and UI placement exactly follow visual/interaction details in the GameUX PRD.

## Phase 3: UI Implementation - Border UI Functionality & Settings

*   (Tasks remain largely the same but assume TTS is used for any relevant auditory feedback from actions)
*   **Task 3.1: Score/Streak Logic**
    *   [x] Keep score and streak state correct and in sync after all player/game actions
    *   [x] Score persists through session (until reset)
    *   [x] Streak resets on incorrect, increments on correct
    *   [x] Visual feedback for score increase (popup '+N' display) overlays ScoreProgressBar in both orientations (Box with min size), and is no longer in GameBorder; streak remains via static counter; loss handled by main feedback. 
*   **Task 3.2: Restart Button Functionality**
    *   [x] Implement restart confirmation dialog ("Are you sure?") before reset; confirmation/reset flow is now exclusively in the Settings dialog footer, not in the border UI.
    *   [x] Reset all game state on restart (score, streak, progress, hints)
    *   [-] Optionally: add TTS confirmation prompt for reset (will not implement; not needed for MVP)
    *   [x] Border restart/reset options and dialog removed for clarity and responsive usability; all reset actions are via modal at end of Settings.
*   **Task 3.3: "I Need Help" Button Functionality**
    *   [x] Hint Tier 1 works (now spells word via TTS—e.g., "C A T" for CAT—and highlights letter in TargetWord, 2025-07-25)
    *   [x] Hint Tier 2 disables/greys out one distractor
    *   [x] Hint button enable/disable logic is correct after every game event
*   **Task 3.4: "Next Word" Button**
    *   [x] Button appears after correct answer if auto-advance is off or timer is running
    *   [x] Immediate manual advance works and resets display state cleanly
    *   [x] Hide button once next word has loaded
*   **Task 3.5: Auto-Advance Timer**
    *   [x] Configurable timer visible/counts down after correct answer
    *   [x] (Moved to 3.6) Timer can be toggled in settings
    *   [x] Correct behavior if interrupted (background, settings change)
*   **Task 3.6: Options/Settings Screen/Dialog**
    *   [x] Implement modal or screen for user to modify:
        *   [x] Timer on/off and duration
        *   [x] Always Show Words Under Images
        *   [x] TTS rate/speed (0.25x - 2.0x range with live display)
        *   [x] Pronounce target word at start of each challenge (default: OFF)
        *   [ ] Other future toggles
*   **Task 3.7: Settings Persistence (Jetpack DataStore)**
    *   [x] Save user settings persistently
    *   [x] Reload settings on app launch
    *   [-] All preference toggles are idempotent and update UI/game immediately (deferred; see 5.3 for enhancement)

## Phase 4: Word Progression & Responsive Layout

*   **Task 4.1: Word List & Progression Logic**
    *   [x] Ensure each correct answer leads to a new (unique and valid) word challenge, ideally a one-letter change from the last correct word (see GameUX §4 §9, PRD §5).
    *   [x] Randomize/shuffle the display order of all image/word choices each round so the correct answer is never in a fixed position.
    *   [x] If word pool is exhausted or can't generate distinct challenge, handle gracefully and let user restart.
    *   [x] Ensure no challenge presents the same word for multiple choices, even if duplicates exist in word list. (Handled by generator logic & duplicate-detection test) 
*   **Task 4.2: Landscape Layout Implementation**
    *   [x] Implement main game and border UI in landscape orientation, verifying that all key elements are faithfully arranged and scale/position as required. (Manual test confirmed 2025-07-22)
    *   [x] Ensure all border buttons, feedback, and indicators function correctly in both orientations (see GameUX §3.1, layout specifics). (Manual test confirmed 2025-07-22)
*   **Task 4.3: Image Assets** ([x] All main word images are now loaded. Placeholders fully replaced for MVP target word set. Further art polish is post-MVP.)
    *   [x] All MVP images and drawables are loaded; landscape/portrait word-under-image UI, hit area, and overlap issues fixed July 2025. Further art polish and asset documentation are post-MVP.
*   **Task 4.4: High Score Support and Persistence**
    *   [x] Track and persist high scores for both Streak and Score.
    *   [x] High scores persist across sessions (saved until reset).
    *   [x] High scores are reset to zero when the user uses the reset feature.
    *   [x] When a player surpasses the previous high score/streak, update and display as new high.
    *   [x] High score/streak is remembered until reset or mistake. Displayed alongside score/streak in UI for now.

*   **Task 4.5: High Score Progress Bar + Display**
    *   [x] Build and show a progress bar reflecting current score progress toward the high score. '+N' popup is overlaid above the progress bar and respects minimum visual area; fully responsive.
    *     [x] The progress bar should be near the full width of the screen on portrait mode horizontally.
    *     [x] Portrait mode bars should fill left-to-right.
    *     [x] For landscape mode, the progress bar should be a vertical bar in the GameBorder on the left side of the screen.
    *     [x] Landscape mode bars should fill bottom-to-top.
    *   [x] Rules of this visual indicator:
    *     [x] In the case of a high score of 0 (reset), the progress can go from 0 (min) to 1 (max)
    *     [x] Otherwise, if the high score is > the current score, the progress bar should go from 0 (min) to the High Score (max).
    *       [x] In this case, the progress fill should be the current score value
    *     [x] If the current score is > the high score, the progress bar should go from 0 (min) to current score (max)
    *       [x] In this case, the progress bar should remain filled completely.
    *     [x] At the far left of the progress bar should be a small area for a current score. It should support numbers up to 99,999.
    *   [x] The rainbow border uses RainbowFull, visible around all progress bars and confirmation dialogs. RainbowMain is removed. All borders/generic gradients now reference RainbowFull for consistency. (2025-07-27)

*   **Task 4.6: High Streak Progress Bar + Display**
    *   [-] Build and show a progress bar reflecting current streak progress toward the high streak. (cancelled, not planned)
    *   [-] Display alongside or underneath streak number in UI. (cancelled, not planned)

*   (Tasks remain largely the same)
*   **Task 4.7: Choice Image Shake Animation**
    *   [x] Enable shake animation for all clicked image choices (both right and wrong picks). (Checkpoint: Now triggers shake on every selection, #2025-07-25)
    *   [x] Keep existing color feedback for correct/wrong but add universal shake effect on press.

*   **Task 4.8: Target Word Tap-to-Spell (Replaces Button)**
    *   [x] Tapping the target word now triggers TTS to spell out the word letter-by-letter (e.g., C A T for CAT), using TtsHelper/GameViewModel.
    *   [x] No additional button is implemented; spell-out occurs via direct word tap for improved child-friendly usability.
    *   [x] This replaces the intended 'speaker icon' button and matches current product UX direction.

*   **Task 4.9: Finalize Game Settings - Reskin Restart Button**
    *   [x] Settings modal fully reskinned (2025-07-27): uses centralized color/theme/rainbow constants, modal and section backgrounds, rainbow borders, and all tab underlines/headers now reflect AccentPink, AccentBlue, AccentPurple.
    *   [x] Tab bar is full-width, all backgrounds/underlines use constants, and all borders use RainbowFull. Confirm/cancel UI dialog uses centralized modal constants.
    *   [x] Game settings properly padded/scoped; Reset confirmation dialog and all modal overlays now match full finalized design and palette.
    *   [x] All previously unfinished modal, underline, and rainbow skinning tasks are complete.

*   **Task 4.10: Settings Modal Tabs**
    *   [x] Four settings modal tabs ('Game', 'Music', 'Speech', 'About') reorganized with proper content distribution:
        *   [x] Game tab: Auto-advance timer, word display options, reset scores functionality
        *   [x] Music tab: Background music volume and track selection
        *   [x] Speech tab: TTS speed control, letter spelling delay, TTS enable/disable 
        *   [x] About tab: App summary, AI disclosure, and dynamic version display (v1.0.1)
    *   [x] Tab colors updated: Pink (Game), Blue (Music), Green (Speech), Purple (About)
    *   [x] Fixed layout issues preventing unwanted scrolling in settings tabs
*   **Task 4.11: Background Music Selection**
    *   [x] Implement background music selection and playback for MP3s. Allow user to choose tracks in Sound tab of settings.
    *   [x] Build out sound settings: master volume, music volume, TTS enable/disable, and music selection options.
    *   [x] Change default background music track to 'chill.mp3'.
    *   [x] Add letter spelling delay slider to Sound settings tab (2025-07-31): Users can now adjust the delay between letters when words are spelled out via TTS, ranging from 500ms to 2000ms in 250ms increments.
*   **Task 4.12: App Icon Migration**
    *   [x] Migrated app icon to a new custom asset, placed app_icon.png in all mipmap-* folders for proper device support and updated manifest reference. Ensured all new icon files are included in git and handled mipmap-anydpi guidance.

*   **Task 4.13: Version Management System**
    *   [x] Implemented automated version display system:
        *   [x] Updated app version to v1.0.1 (versionCode 2) in build.gradle.kts
        *   [x] Added dynamic version display in About tab that reads from app package info
        *   [x] Version automatically updates in UI when build version is changed
        *   [x] Positioned version display in bottom-right corner of About settings tab

## Phase 5: Refinement & Testing

*   **Task 5.1: TTS Refinements & Optional Sound Effects**
    *   [x] Test TTS pronunciation clarity and timing across different scenarios.
    *   [x] Adjust TTS parameters (pitch, rate) if necessary for better experience.
    *     [x] Slow down the spelling speed - it's pretty fast right now. (2025-07-28): Now each letter is spoken by TTS with a ~750ms pause between, implemented via coroutine per-letter in GameViewModel.)
    *     [x] Make letter spelling delay user-configurable (2025-07-31): Added slider in Audio settings tab allowing users to adjust delay between letters from 500ms to 2000ms in 250ms increments. Default remains 750ms.
    *     [x] Add TTS speed control (2025-08-01): Implemented configurable TTS speech rate from 0.25x to 2.0x speed with live display in Speech settings tab. All TTS calls now respect user's preferred speech rate.
    *   [ ] (Post-MVP or iterative) Consider adding subtle, non-verbal sound effects for UI interactions (button clicks, animations) if desired, to complement TTS.
*   **Task 5.2: Thorough Testing** (Includes testing TTS functionality thoroughly)
*   **Task 5.3: UI Polish & Animations**
    *   [x] Improve default font for child readability and letterform distinction (e.g. Comic Neue Bold—clearer I/l differentiation)
    *   [x] Settings toggles update UI/game immediately as toggled (idempotent and fully live; all toggles/sliders now update instantly, dialog no longer has OK/Cancel).
    *   [x] All main button and background drawables loaded for MVP; further icon/layout polish post-MVP.
    *   [ ] Score progress bar (% to high score + % to high streak and then marking them as they pass)
    *   [x] Re-lay out image choice to 2 horizontal/1 horizontal and finalize all borders, spacing, and cartoon/correctness highlight for MVP. Dashed/stateful borders, margin/padding fixes, and all feedback placement are complete. (2025-07-24)
    *   [x] Reskin the entire app
    *   [x] Target word sizing landscape/portrait, no-gap logic, and image-choice layout margins finalized for MVP (July 2025).
    *   [x] Reskin options modal + the reset modal. Settings now use:
        * Discrete slider for Auto-Advance (Off/3/5/8/15/30s), pink color, compact
        * Checkboxes now pink, compact, left of text
        * Setting dialog remains open on rotation (rememberSaveable)
        * All layout/padding minimized for mobile UX
    *   [x] Add pulsing animation to "Next Word" button, and remove all text-based score/streak counters from GameBorder for a cleaner, visual-only layout.
*   **Task 5.4: Code Cleanup & Documentation**

## Corrections, Known Issues, and Pending Feature Tasks (pre-polish)

*   [x] TTS pronounces POP as an acronym instead of 'pop': Fixed by lowercasing only words that are fully uppercase (acronyms) and more than one letter before sending them to TTS, ensuring target/image words are pronounced correctly and general phrases/messages retain their intended casing.
*   [x] Prevent the words from being pronounced when the device is rotated.
*   [x] Ensure the word is pronounced on the first display of the app (if setting enabled to do so).
*   [x] Stop the game from auto-progressing when the app is backgrounded.
*   [ ] Explore using the phonetic alphabet for sound matching (PRD section 7 & undecided/logic).
*   [ ] Explore indices instead of letters for sound matching (PRD section 7 & undecided/logic).
*   [ ] Explore recorded words instead of text-to-speech.
*   [x] Displaying score and streak prominently per GameUX.
*   [x] Add a "Next Word" button to bypass the auto-advance timer after a correct answer.
*   [ ] Review all PRD_GameUX details and complete any missing subtlety, especially:
    *   Visual feedback: star animation, shake effect, feedback sound for right/wrong.
    *   "Restart" should require a confirmation popup.
    *   Option for "Always Show Words Under Images" (toggle in Options).
    *   Options/settings dialog responsive placement. (Dialog now uses vertical scroll with restart modal at footer; all settings and actions always accessible on all screen sizes/orientations.)
    *   Support for landscape layout and border UI in all modes.
*   [ ] (General) Sweep for any additional polish or small gaps left in UI/logic per PRD/UX checklist.

## General Considerations Throughout Development:
*   (Same as before)

---

**Workflow Note:**
- Always update and check-in PRD_DevelopmentPlan.md before every code commit to ensure plan/actual status stays in sync.
- 2025-07-29: minSdkVersion lowered to 30 for device compatibility; major fixes to image/word/choice scaling logic in MainActivity.kt for landscape and portrait on both phones and tablets. Responsive UI should now behave as intended on all QA devices.
**Tracking Legend:**

*   `[ ]` - To Do
*   `[x]` - Done
*   `[ / ]` - In Progress
*   `[-]` - Blocked / On Hold
