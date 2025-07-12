# TinyWords - MVP Development Plan

This document outlines the development plan for the Minimum Viable Product (MVP) of the TinyWords game, based on the specifications in `PRD_GameUX.md`.

## Phase 1: Project Setup & Core Structure

*   **Task 1.1: New Project Creation**
    *   [x] Set up a new Android Studio project.
    *   [/] Configure `build.gradle` with necessary dependencies (e.g., Kotlin Coroutines, Jetpack Compose, ViewModel, LiveData/StateFlow, Jetpack DataStore) - *Ongoing as features are added*.
*   **Task 1.2: Basic Asset & Initial TTS Management**
    *   [x] Create placeholder images for the three choices (`placeholder_1.png`, `placeholder_2.png`, `placeholder_3.png` added to `res/drawable`).
    *   [x] Implement a basic Text-To-Speech (TTS) helper class for word pronunciations (target words, "Correct!", "Try Again").
        *   [x] Handle TTS engine initialization and lifecycle.
    *   [/] Set up `strings.xml` for all user-facing text (initial target words, button labels, messages, TTS phrases like "Correct", "Try Again").
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
    *   [/] Implement basic functions in `GameViewModel` (most logic complete – hint & reset remain):
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
    *   [x] Grey out and disable any image choice immediately after an incorrect tap, as per PRD feedback flow.
*   **Task 2.4: Feedback Implementation (Visual & TTS Auditory)**
    *   [ ] Visual feedback: dynamic border, animations (shake/star).
    *   [ ] Auditory feedback: Trigger TTS for "Correct!" / "Try Again" / selected word pronunciation via `GameViewModel`.
    *   [ ] After each incorrect selection, visually grey out (desaturate) only the chosen image and disable its tap, as per GameUX feedback.
    *   [ ] Show words under images after correct or incorrect answers, with only the differing letter highlighted (green for correct, red for incorrect), following the stepwise feedback sequence described in the PRD.
*   **Task 2.5: Border UI Elements (Static Display & Feedback Logic)**
    *   [x] Composables for `ScoreStreakDisplay`, `RestartButton`, `HelpButton`, `OptionsButton`.
    *   [x] Position within `GameBorder`. Display initial values from `GameViewModel`.
    *   [ ] Implement logic for "I Need Help" button: enabled at the start of each new word, disabled after using Tier 2, re-enabled for the next word, matching UX rules.
    *   [ ] Ensure feedback animations (shake, star burst, border color) and UI placement exactly follow visual/interaction details in the GameUX PRD.

## Phase 3: UI Implementation - Border UI Functionality & Settings

*   (Tasks remain largely the same but assume TTS is used for any relevant auditory feedback from actions)
*   **Task 3.1: Score/Streak Logic**
*   **Task 3.2: Restart Button Functionality** (TTS confirmation for "Are you sure?" is optional, standard dialog is fine)
*   **Task 3.3: "I Need Help" Button Functionality**
*   **Task 3.4: "Next Word" Button**
*   **Task 3.5: Auto-Advance Timer**
*   **Task 3.6: Options/Settings Screen/Dialog**
*   **Task 3.7: Settings Persistence (Jetpack DataStore)**

## Phase 4: Word Progression & Responsive Layout

*   (Tasks remain largely the same)
*   **Task 4.1: Word List & Progression Logic**
*   **Task 4.2: Landscape Layout Implementation**
*   **Task 4.3: Image Assets** ([ ] (Post-MVP or iterative) Replace placeholders. For MVP, placeholders are fine.)

## Phase 5: Refinement & Testing

*   **Task 5.1: TTS Refinements & Optional Sound Effects**
    *   [ ] Test TTS pronunciation clarity and timing across different scenarios.
    *   [ ] Adjust TTS parameters (pitch, rate) if necessary for better experience.
    *   [ ] (Post-MVP or iterative) Consider adding subtle, non-verbal sound effects for UI interactions (button clicks, animations) if desired, to complement TTS.
*   **Task 5.2: Thorough Testing** (Includes testing TTS functionality thoroughly)
*   **Task 5.3: UI Polish & Animations**
*   **Task 5.4: Code Cleanup & Documentation**

## General Considerations Throughout Development:
*   (Same as before)

---

**Workflow Note:**
- Always update and check-in PRD_DevelopmentPlan.md before every code commit to ensure plan/actual status stays in sync.

**Tracking Legend:**

*   `[ ]` - To Do
*   `[/]` - In Progress
*   `[x]` - Done
*   `[-]` - Blocked / On Hold