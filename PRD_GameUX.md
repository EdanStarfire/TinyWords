# TinyWords - Game User Experience (UX)

## 1. Overview

*   **Game Concept:** A touch-based game for young children to learn how small letter changes affect words and their sounds. Players choose an image that matches a target word, where options are slight variations.
*   **Target Audience:** Young children (e.g., preschool, kindergarten) learning early literacy skills.
*   **Learning Goal:** Help children recognize phonetic patterns and how individual letter sounds contribute to word formation.

## 2. Core Game Loop

1.  A **Target Word** is displayed (e.g., "CAT").
2.  Three **Image Choices** are presented. Words beneath images are hidden by default (unless "Always Show Words" setting is On).
3.  The "I Need Help" button is enabled (if Tier 2 hint not yet used for this word).
4.  The player **taps an image**.
5.  **Feedback** is provided based on the choice.
6.  If **correct**:
    *   Visual and auditory correct feedback is given.
    *   Words appear under all three choice images (if not already visible). The differing letter is highlighted (green for correct, red for incorrect alternatives).
    *   A "Next Word" button appears. "I Need Help" button is disabled (or re-evaluates for next word state).
    *   The game auto-advances to a new **Target Word** after a timer (configurable, e.g., 5 seconds) or when the "Next Word" button is tapped (unless auto-advance is disabled in settings). This new word is a one-letter variation of the previously *correctly identified* word.
7.  If **incorrect**:
    *   Visual and auditory incorrect feedback is given.
    *   The word appears under the selected incorrect image (if not already visible), with the differing letter highlighted in red.
    *   The selected incorrect image is greyed out (desaturated) and becomes non-tappable.
    *   Player is encouraged to try another choice from the remaining active options. "I Need Help" button remains available if Tier 2 hint not yet used.

## 3. Screen Designs & UI Elements

### 3.1. Main Game Screen

*   **General Layout:**
    *   A distinct border surrounds the entire interactive game area.
    *   This border area also contains UI elements for "Score/Streak", "Restart", "Help", and "Options".
*   **Target Word Display:**
    *   **Style:** Large, clear, sans-serif font.
    *   **Interaction:** Read-only, word pronounced on appearance. Subject to highlighting by Tier 1 Hint.
*   **Image Choices (3 options):**
    *   **Style:** Simple, clear illustrations.
    *   **Word Display:** Text for the word corresponding to each image is hidden by default during selection, unless "Always Show Words Under Images" setting is enabled. Text appears/is highlighted based on interaction (see Section 2, Points 6 & 7).
    *   **Interaction:** Tappable. If an incorrect choice is made, that choice becomes non-tappable and visually greyed out.
*   **Feedback Animations/Sounds:**
    *   **Correct:**
        *   Visual: Green highlight/border around the chosen image, star animation. Words under all choice images become/remain visible, with the key letter in the correct choice highlighted green, and key letters in incorrect choices highlighted red.
        *   Auditory: Cheerful sound, "Correct!" spoken, word pronounced.
    *   **Incorrect:**
        *   Visual: Red highlight/border around the chosen image, slight shake. The chosen image is then greyed out. The word under the chosen image becomes/remains visible, with the key incorrect letter highlighted red.
        *   Auditory: Gentle "Try again" sound, incorrect word pronounced.
*   **Border UI Elements:**
    *   **Score/Streak Counter:**
        *   **Content:** Displays "Number correct in a row" (e.g., "Streak: 5").
        *   **Position:** Consistently placed (e.g., top-left).
    *   **Restart Button:**
        *   **Icon/Text:** Common restart icon or "Restart".
        *   **Action:** Resets game. Requires confirmation ("Are you sure?").
        *   **Position:** Consistently placed (e.g., top-right).
    *   **"I Need Help" / Hint Button:**
        *   **Icon/Text:** Question mark icon or "Help".
        *   **Availability:** Enabled at the start of each new word. Disabled for the current word after Tier 2 hint is used. Re-enabled for the next word. Using hints does not affect score/streak.
        *   **Action (Tiered):**
            *   **Tier 1 Hint:** Highlights the letter in the main **Target Word display** that is the focus of phonetic change for the current choices. (e.g., If Target is "CAR", and choices involve changing the first letter, the 'C' in "CAR" is highlighted).
            *   **Tier 2 Hint:** One incorrect image choice is greyed out and becomes non-tappable.
        *   **Position:** Consistently placed (e.g., bottom-center).
    *   **Options/Settings Button:**
        *   **Icon/Text:** Gear icon or "Options".
        *   **Action:** Opens a settings dialog/screen allowing modification of:
            *   Auto-Advance Timer duration (e.g., 3s, 5s, 8s, 10s, Disable).
            *   Always Show Words Under Images (Toggle On/Off, default Off).
        *   **Position:** Consistently placed (e.g., bottom-corner).
*   **"Next Word" Button:**
    *   **Appearance:** After a correct answer.
    *   **Action:** Advances immediately, bypassing auto-advance timer.
    *   **Positioning:** Consistent spot (e.g., near bottom center or last correct image).

---

**Layout Specifics:** (Remains the same - examples: Portrait: Target top, Images bottom; Landscape: Target left, Images right. Border UI elements consistently placed within main border.)

**A. Portrait Mode:** ...
**B. Landscape Mode:** ...

---
*   **(Optional) Progress Indicator:** ...
*   **(Optional) Sound Toggle:** (Could also be in Options menu)

## 4. Word Progression Logic

*   (Remains largely the same, focusing on one-letter changes from previous correct word, and incorrect choices being variations of current target. Example flow adjusted for new feedback.)
*   Example Flow:
    1.  Target: "CAT". Choices: Images for "HAT", "CAT", "BAT". (Words hidden by default).
        *   Player taps "Help" (Tier 1): 'C' in displayed "CAT" is highlighted.
        *   Player taps image for "HAT" (incorrect).
            *   Feedback: "HAT" image red flash, greys out. Word "HAT" appears below it, 'H' red.
        *   Player taps "Help" (Tier 2): Image for "BAT" greys out.
        *   Player taps image for "CAT" (correct).
            *   Feedback: "CAT" image green highlight. Words "HAT", "CAT", "BAT" appear/remain. 'H' (red), 'C' (green), 'B' (red) highlighted. "Next Word" button appears.
    2.  Auto-advances or player taps "Next Word". New Target: "CAR"...

## 5. Visual & Auditory Style

*   (Remains the same - Bright, friendly, high-contrast, clear pronunciations, positive/gentle feedback.)

## 6. Undecided UX Decisions / Points for Discussion

*   **Hint Tier 1 - Exact Highlighting Logic for Target Word:**
    *   If current target is "CAR" (from previous "CAT"), does Tier 1 highlight the 'R' in "CAR" (the new letter)? Or the 'C' (the letter that *will be varied* by incorrect choices like "BAR", "JAR")? *Decision: Highlight the letter position that varies among the choices for the current target word.*
*   **Word List Generation/Source:** Crucial for appropriate difficulty and logical progression.
*   **Exact Placement of Border UI Elements:** Finalize positions for all (Score, Restart, Help, Options).
*   **Initial state of "Always Show Words" setting:** Default to OFF confirmed.
*   **Initial state of "Auto-Advance Timer":** Default to e.g., 5 seconds.

## 7. Future Considerations (Post MVP)

*   Vowel changes.
*   Consonant blends and digraphs.
*   Multiple difficulty levels.
*   Tracking scores/achievements (beyond streak).
*   Parental controls (perhaps locking settings).
*   More granular sound controls (e.g., voice vs. SFX toggle in Options).
*   Lives (# wrong before streak over)
