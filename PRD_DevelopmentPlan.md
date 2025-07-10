# TinyWords - MVP Development Plan

This document outlines the development plan for the Minimum Viable Product (MVP) of the TinyWords game, based on the specifications in `PRD_GameUX.md`.

## Phase 1: Project Setup & Core Structure

*   **Task 1.1: New Project Creation**
    *   [x] Set up a new Android Studio project.
    *   [/] Configure `build.gradle` with necessary dependencies (e.g., Kotlin Coroutines, Jetpack Compose, ViewModel, LiveData/StateFlow, Jetpack DataStore) - *Ongoing as features are added*.
*   **Task 1.2: Basic Asset & Initial TTS Management**
    *   [x] Create placeholder images for the three choices (`placeholder_1.png`, `placeholder_2.png`, `placeholder_3.png` added to `res/drawable`).
    *   [X] Implement a basic Text-To-Speech (TTS) helper class for word pronunciations (target words, "Correct!", "Try Again").
        *   [X] Handle TTS engine initialization and lifecycle.
    *   [/] Set up `strings.xml` for all user-facing text (initial target words, button labels, messages, TTS phrases like "Correct", "Try Again").
*   **Task 1.3: Data Structures for Words**
    *   [X] Define how the word list will be stored (e.g., hardcoded list, JSON in assets).
    *   [X] Define data class for a "Game Round" or "Word Challenge" (e.g., `WordChallenge(targetWord: String, correctImageWord: String, incorrectImageWord1: String, incorrectImageWord2: String, targetImageRes: Int, incorrect1ImageRes: Int, incorrect2ImageRes: Int)`).
*   **Task 1.4: Core Game Logic - State Management**
    *   [X] Create `GameViewModel` (extending `androidx.lifecycle.ViewModel`).
    *   [X] Inject or provide access to the TTS helper in `GameViewModel`.
    *   [X] Define state holders in `GameViewModel`:
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
        *   [ ] `requestHint()`
        *   [ ] `resetGame()`
        *   [x] `startAutoAdvanceTimer()` / `cancelAutoAdvanceTimer()`
        *   [x] `updateSettings(newSettings: GameSettings)`

## Phase 2: UI Implementation - Main Game Screen (Portrait First with Jetpack Compose)

*   **Task 2.1: Basic Layout (Portrait)**
    *   [ ] Create main activity & Jetpack Compose content view.
    *   [ ] Implement portrait layout: `TargetWordArea`, `ImageChoicesArea`, `GameBorder`, internal dividers.
*   **Task 2.2: Target Word Display & Pronunciation**
    *   [ ] Composable for target word from `GameViewModel`.
    *   [ ] Trigger TTS pronunciation of the target word via `GameViewModel` when it appears/changes.
    *   [ ] Implement dynamic highlighting for Hint Tier 1.
*   **Task 2.3: Image Choices Display & Interaction**
    *   [ ] Reusable Composable for a single image choice.
    *   [ ] Display three images from `GameViewModel`.
    *   [ ] Implement tap handling, linking to `GameViewModel.processPlayerChoice()`.
*   **Task 2.4: Feedback Implementation (Visual & TTS Auditory)**
    *   [ ] Visual feedback: dynamic border, animations (shake/star).
    *   [ ] Auditory feedback: Trigger TTS for "Correct!" / "Try Again" / selected word pronunciation via `GameViewModel`.
    *   [ ] Logic for greying out & disabling incorrect choices.
    *   [ ] Logic for showing/hiding words under images & highlighting letters.
*   **Task 2.5: Border UI Elements (Static Display)**
    *   [ ] Composables for `ScoreStreakDisplay`, `RestartButton`, `HelpButton`, `OptionsButton`.
    *   [ ] Position within `GameBorder`. Display initial values from `GameViewModel`.

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

**Tracking Legend:**

*   `[ ]` - To Do
*   `[/]` - In Progress
*   `[x]` - Done
*   `[-]` - Blocked / On Hold