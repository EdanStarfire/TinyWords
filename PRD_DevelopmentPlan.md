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
        *   [x] `resetGame()`
        *   [x] `startAutoAdvanceTimer()` / `cancelAutoAdvanceTimer()`
        *   [x] `updateSettings(newSettings: GameSettings)`
    *   [x] Update WordChallengeGenerator to disable randomness for testing purposes.

## Phase 2: UI Implementation - Main Game Screen (Portrait First with Jetpack Compose)

*   **Task 2.1: Basic Layout (Portrait)**
    *   [x] Create main activity & Jetpack Compose content view.
    *   [x] Implement portrait layout: `TargetWordArea`, `ImageChoicesArea`, `GameBorder`, internal dividers.
*   **Task 2.2: Target Word Display & Pronunciation**
    *   [x] Composable for target word from `GameViewModel`.
    *   [x] Trigger TTS pronunciation of the target word via `GameViewModel` when it appears/changes.
    *   [x] Implement dynamic highlighting for Hint Tier 1, matching PRD logic (highlight the differing letter position among image choices for the current target word).
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
    *   [x] Visual feedback for score increase (popup '+N' display); streak done via static counter; loss handled by main feedback. 
*   **Task 3.2: Restart Button Functionality**
    *   [x] Implement restart confirmation dialog ("Are you sure?") before reset
    *   [x] Reset all game state on restart (score, streak, progress, hints)
    *   [-] Optionally: add TTS confirmation prompt for reset (will not implement; not needed for MVP)
*   **Task 3.3: "I Need Help" Button Functionality**
    *   [x] Hint Tier 1 works (TargetWord letter highlight)
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
        *   [ ] TTS rate
        *   [x] Pronounce target word at start of each challenge (default: OFF)
        *   [ ] Other future toggles
*   **Task 3.7: Settings Persistence (Jetpack DataStore)**
    *   [x] Save user settings persistently
    *   [x] Reload settings on app launch
    *   [-] All preference toggles are idempotent and update UI/game immediately (deferred; see 5.3 for enhancement)

## Phase 4: Word Progression & Responsive Layout

*   (Tasks remain largely the same)
*   **Task 4.1: Word List & Progression Logic**
    *   [ ] Ensure each correct answer leads to a new (unique and valid) word challenge, ideally a one-letter change from the last correct word (see GameUX §4 §9, PRD §5).
    *   [ ] Randomize/shuffle the display order of all image/word choices each round so the correct answer is never in a fixed position.
    *   [ ] If word pool is exhausted or can't generate distinct challenge, handle gracefully and let user restart. 
*   **Task 4.2: Landscape Layout Implementation**
    *   [ ] Implement main game and border UI in landscape orientation, verifying that all key elements are faithfully arranged and scale/position as required.
    *   [ ] Ensure all border buttons, feedback, and indicators function correctly in both orientations (see GameUX §3.1, layout specifics).
*   **Task 4.3: Image Assets** ([ ] (Post-MVP or iterative) Replace placeholders. For MVP, placeholders are fine.)
    *   [ ] Abstract or document the process for preparing non-placeholder/illustration assets post-MVP so art/sound handoff is clear.

## Phase 5: Refinement & Testing

*   **Task 5.1: TTS Refinements & Optional Sound Effects**
    *   [ ] Test TTS pronunciation clarity and timing across different scenarios.
    *   [ ] Adjust TTS parameters (pitch, rate) if necessary for better experience.
    *   [ ] (Post-MVP or iterative) Consider adding subtle, non-verbal sound effects for UI interactions (button clicks, animations) if desired, to complement TTS.
*   **Task 5.2: Thorough Testing** (Includes testing TTS functionality thoroughly)
*   **Task 5.3: UI Polish & Animations**
    *   [ ] Settings toggles update UI/game immediately as toggled (make idempotent and live, not on OK only, from 3.7)
*   **Task 5.4: Code Cleanup & Documentation**

## Corrections, Known Issues, and Pending Feature Tasks (pre-polish)

*   [x] Prevent the words from being pronounced when the device is rotated.
*   [ ] Ensure the word is pronounced on the first display of the app.
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
    *   Options/settings dialog responsive placement.
    *   Support for landscape layout and border UI in all modes.
*   [ ] (General) Sweep for any additional polish or small gaps left in UI/logic per PRD/UX checklist.

## General Considerations Throughout Development:
*   (Same as before)

---

**Workflow Note:**
- Always update and check-in PRD_DevelopmentPlan.md before every code commit to ensure plan/actual status stays in sync.

**Tracking Legend:**

*   `[ ]` - To Do
*   `[x]` - Done
*   `[ / ]` - In Progress
*   `[-]` - Blocked / On Hold
