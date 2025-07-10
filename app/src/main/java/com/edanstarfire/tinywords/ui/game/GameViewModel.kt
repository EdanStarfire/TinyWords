package com.edanstarfire.tinywords.ui.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Important for launching coroutines
// Make sure to import your TtsHelper class
import com.edanstarfire.tinywords.tts.TtsHelper
import com.edanstarfire.tinywords.WordChallengeGenerator
import com.edanstarfire.tinywords.WordChallenge
import com.edanstarfire.tinywords.WordDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Marks this ViewModel for Hilt injection
class GameViewModel @Inject constructor(
    private val ttsHelper: TtsHelper,
    private val wordChallengeGenerator: WordChallengeGenerator
) : ViewModel() {
    // Expose TTS readiness to the UI or for internal ViewModel logic
    val isTtsReady: StateFlow<Boolean> = ttsHelper.isInitialized
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep subscribed for 5s after last observer
            initialValue = false // Assume not ready initially
        )

    // 1. Current target word & image choices (combined into a CurrentChallenge state)
    private val _currentChallenge = MutableStateFlow<WordChallenge?>(null)
    val currentChallenge: StateFlow<WordChallenge?> = _currentChallenge.asStateFlow()

    // 2. Correct/incorrect status
    private val _feedbackState = MutableStateFlow<GameFeedback>(GameFeedback.None)
    val feedbackState: StateFlow<GameFeedback> = _feedbackState.asStateFlow()

    // 3. Score/streak
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    // 4. Hint status
    // Example: 0 = No hint, 1 = Tier 1 hint active (e.g. highlight letters), 2 = Tier 2 hint (e.g. show word)
    private val _hintLevel = MutableStateFlow(0)
    val hintLevel: StateFlow<Int> = _hintLevel.asStateFlow()

    private val _isHintButtonEnabled = MutableStateFlow(true) // Can hints be requested now?
    val isHintButtonEnabled: StateFlow<Boolean> = _isHintButtonEnabled.asStateFlow()

    // 5. Timer state (for auto-advance or timed challenges)
    private val _timerValueSeconds = MutableStateFlow(30) // Example: 30 seconds
    val timerValueSeconds: StateFlow<Int> = _timerValueSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // To hold the reference to the current timer job so it can be cancelled
    private var autoAdvanceJob: Job? = null

    // 6. Settings values
    private val _gameSettings = MutableStateFlow(GameSettings()) // Default settings
    val gameSettings: StateFlow<GameSettings> = _gameSettings.asStateFlow()


    init {
        Log.i("GameViewModel", "GameViewModel initialized with TtsHelper and WordChallengeGenerator.")

        viewModelScope.launch {
            _gameSettings.collect { settings ->
                Log.i("GameViewModel", "Game settings updated: $settings")
                // If auto-advance was turned off while a timer was running, cancel it.
                if (!settings.autoAdvance) {
                    cancelAutoAdvanceTimer()
                }
                // If auto-advance is turned ON, and we are in a state where a timer SHOULD have been running
                // (e.g., just after a correct answer), we might need to initiate it.
                // However, the primary trigger for the timer will be a correct answer.
            }
        }

        viewModelScope.launch {
            isTtsReady.collect { ready ->
                Log.i("GameViewModel", "TTS readiness changed: $ready")
            }
        }
        // Load the first word challenge when the ViewModel is ready
        loadNewWordChallenge()
    }

    fun loadNewWordChallenge() {
        Log.d("GameViewModel", "Loading new word challenge...")
        cancelAutoAdvanceTimer()

        val challenge = wordChallengeGenerator.getRandomInitialChallenge()
        _currentChallenge.value = challenge
        _feedbackState.value = GameFeedback.None

        challenge?.let {
            pronounceWord(it.targetWord)
            Log.i("GameViewModel", "New word loaded: ${it.targetWord}.")
        } ?: Log.w("GameViewModel", "No new word challenge could be loaded.")
    }

    fun processPlayerChoice(selectedWord: String) {
        val challenge = _currentChallenge.value
        Log.d("GameViewModel", "Processing player choice: $selectedWord, Challenge: $challenge")
        autoAdvanceJob?.cancel()
        _isTimerRunning.value = false

        if (challenge == null) {
            Log.w("GameViewModel", "No active challenge. Cannot process choice.")
            return
        }

        val isCorrect = selectedWord == challenge.correctImageWord

        if (isCorrect) {
            _feedbackState.value = GameFeedback.Correct(selectedWord)
            _score.value += 10
            _streak.value += 1
            if (_gameSettings.value.autoAdvance) {
                startAutoAdvanceTimer()
            } else {
                Log.i("GameViewModel", "Correct answer. Auto-advance is off. Waiting for manual next.")
            }
        } else {
            _feedbackState.value = GameFeedback.Incorrect(selectedWord)
            _streak.value = 0
            Log.i("GameViewModel", "Incorrect answer.")
        }
    }

    // Call this when a correct answer is given AND auto-advance is enabled.
    private fun startAutoAdvanceTimer() {
        autoAdvanceJob?.cancel() // Cancel any unlikely existing job (shouldn't be necessary here but safe)

        val currentSettings = _gameSettings.value
        if (!currentSettings.autoAdvance) {
            Log.i("GameViewModel", "Auto-advance is disabled. Timer not started.")
            _isTimerRunning.value = false
            _timerValueSeconds.value = 0
            return
        }

        _isTimerRunning.value = true
        val intervalSeconds = currentSettings.autoAdvanceIntervalSeconds
        _timerValueSeconds.value = intervalSeconds // Initialize countdown display

        autoAdvanceJob = viewModelScope.launch {
            Log.i("GameViewModel", "Auto-advance timer started for $intervalSeconds seconds (after correct answer).")
            for (i in intervalSeconds downTo 1) {
                delay(1000L)
                _timerValueSeconds.value = i - 1
                if (!_isTimerRunning.value) { // Check if cancelled externally (e.g., settings changed)
                    Log.i("GameViewModel", "Auto-advance timer was cancelled during countdown.")
                    _timerValueSeconds.value = 0
                    return@launch
                }
            }
            // Timer finished
            if (_isTimerRunning.value) { // Ensure it wasn't cancelled right at the end
                Log.i("GameViewModel", "Auto-advance timer finished. Loading next word.")
                _isTimerRunning.value = false // Reset before loading next
                _timerValueSeconds.value = 0
                loadNewWordChallenge()
            }
        }
    }

    fun cancelAutoAdvanceTimer() {
        autoAdvanceJob?.cancel()
        _isTimerRunning.value = false
        _timerValueSeconds.value = 0 // Reset countdown display
        Log.i("GameViewModel", "Auto-advance timer cancelled explicitly.")
    }

    // This function is now more about updating the _gameSettings StateFlow.
    // The collectLatest block on _gameSettings will handle reacting to the change.
    fun updateSettings(newSettings: GameSettings) {
        Log.i("GameViewModel", "ViewModel: Updating game settings to: $newSettings")
        val oldSettings = _gameSettings.value
        _gameSettings.value = newSettings

        // If auto-advance was just turned OFF and a timer was running, cancel it.
        if (oldSettings.autoAdvance && !newSettings.autoAdvance && _isTimerRunning.value) {
            cancelAutoAdvanceTimer()
            Log.i("GameViewModel", "Auto-advance turned off during a running timer. Timer cancelled.")
        }
        // If auto-advance was just turned ON, and the player is currently in a state
        // where they have just answered correctly (and no timer is running because it was previously off),
        // we might want to start the timer.
        // This case is tricky without knowing the exact game state (e.g. are we waiting after a correct answer?).
        // For simplicity, the timer will start on the *next* correct answer if auto-advance is now on.
        // Or, if you have a specific UI element (like a "Next Word" button that becomes a timer display),
        // you could trigger `startAutoAdvanceTimer()` from the UI if `_feedbackState` is `Correct`
        // and `newSettings.autoAdvance` is true and `!_isTimerRunning.value`.

        // Current approach: Settings change primarily affects future timer starts
        // or cancels an active one if auto-advance is disabled.
    }

    fun pronounceWord(word: String) {
        // TtsHelper now internally checks if it's initialized before speaking
        // So, direct call is safer. But you could still add a check here if needed.
        if (isTtsReady.value) { // Optional: Check here too if you want GameViewModel to be aware
            ttsHelper.speak(word)
        } else {
            Log.w("GameViewModel", "Attempted to pronounceWord, but TTS is not ready. Word: $word")
            // You could queue the word here to be spoken once TTS is ready,
            // or inform the user.
        }
    }

    // Call this if the player manually chooses to go to the next word
    // (e.g. presses a "Next Word" button when auto-advance is off or they want to skip the timer)
    fun requestNextWordManually() {
        Log.i("GameViewModel", "Manual request for next word.")
        cancelAutoAdvanceTimer() // Stop any current auto-advance timer
        loadNewWordChallenge()
    }

    override fun onCleared() {
        super.onCleared()
        autoAdvanceJob?.cancel() // Ensure timer is cancelled when ViewModel is cleared
        Log.i("GameViewModel", "GameViewModel cleared.")
    }

    // ... Your game state holders and other logic will go here ...
}

// Placeholder for game feedback
sealed class GameFeedback {
    data object None : GameFeedback() // No feedback yet or after feedback displayed
    data class Correct(val chosenWord: String) : GameFeedback()
    data class Incorrect(val chosenWord: String) : GameFeedback()
}

// Placeholder for Game Settings (you'll load this from DataStore later)
data class GameSettings(
    val ttsSpeed: Float = 1.0f,
    val autoAdvance: Boolean = true,
    val autoAdvanceIntervalSeconds: Int = 30,
    val hintLevelAllowed: Int = 2 // e.g., 0=none, 1=highlight, 2=show word
    // Add other settings as needed
)
