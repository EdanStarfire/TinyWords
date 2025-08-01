package com.edanstarfire.tinywords.ui.game

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.IOException

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
import com.edanstarfire.tinywords.ScoreStreakRepository
import com.edanstarfire.tinywords.GameSettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Serializable
data class SpokenContent(
    val positive: List<String> = emptyList(),
    val encouragement: List<String> = emptyList()
)

@HiltViewModel // Marks this ViewModel for Hilt injection
class GameViewModel @Inject constructor(
    private val ttsHelper: TtsHelper,
    private val wordChallengeGenerator: WordChallengeGenerator,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: Context
) : ViewModel() {
    private val recentTargetWords = ArrayDeque<String>(10)
    private var lastPronouncedTarget: String? = null

    private val scoreStreakRepo = ScoreStreakRepository(appContext)
    private val gameSettingsRepo = GameSettingsRepository(appContext)
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

    // 3. Score/streak/high scores
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()
    private val _scoreHigh = MutableStateFlow(0)
    val scoreHigh: StateFlow<Int> = _scoreHigh.asStateFlow()
    private val _streakHigh = MutableStateFlow(0)
    val streakHigh: StateFlow<Int> = _streakHigh.asStateFlow()
    private val _streakDelta = MutableStateFlow<Int?>(null)
    val streakDelta: StateFlow<Int?> = _streakDelta.asStateFlow()

    // 4. Hint status
    // Example: 0 = No hint, 1 = Tier 1 hint active (e.g. highlight letters), 2 = Tier 2 hint (e.g. show word)
    private val _hintLevel = MutableStateFlow(0)
    val hintLevel: StateFlow<Int> = _hintLevel.asStateFlow()

    // Tracks incorrect guesses and hints this round for adaptive scoring
    private var incorrectCount = 0
    private var hintCount = 0

    // Tracks which words are currently disabled for this round (wrong guesses, hints)
    private val _disabledWords = MutableStateFlow<Set<String>>(emptySet())
    val disabledWords: StateFlow<Set<String>> = _disabledWords.asStateFlow()

    private val _isHintButtonEnabled = MutableStateFlow(true) // Can hints be requested now?
    val isHintButtonEnabled: StateFlow<Boolean> = _isHintButtonEnabled.asStateFlow()

    // 5. Timer state (for auto-advance or timed challenges)
    private val _timerValueSeconds = MutableStateFlow(30) // Example: 30 seconds
    val timerValueSeconds: StateFlow<Int> = _timerValueSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // To hold the reference to the current timer job so it can be cancelled
    private var autoAdvanceJob: Job? = null
    private var encouragementJob: Job? = null

    // 6. Settings values
    private val _gameSettings = MutableStateFlow(GameSettings()) // Default settings
    val gameSettings: StateFlow<GameSettings> = _gameSettings.asStateFlow()
    private var initialGameSettingsLoaded = false

    // --- Spoken feedback loaded from assets/spoken_content.json ---
    private var spokenContent: SpokenContent = SpokenContent()

    init {
        loadSpokenContent()

        // Load score/streak/high scores from DataStore
        viewModelScope.launch {
            scoreStreakRepo.score.collect { _score.value = it }
        }
        viewModelScope.launch {
            scoreStreakRepo.streak.collect { _streak.value = it }
        }
        viewModelScope.launch {
            scoreStreakRepo.scoreHigh.collect { _scoreHigh.value = it }
        }
        viewModelScope.launch {
            scoreStreakRepo.streakHigh.collect { _streakHigh.value = it }
        }

        // Load settings from DataStore
        viewModelScope.launch {
            gameSettingsRepo.settings.collect { loadedSettings ->
                if (!initialGameSettingsLoaded) {
                    initialGameSettingsLoaded = true
                    _gameSettings.value = loadedSettings
                }
            }
        }

        Log.i("GameViewModel", "GameViewModel initialized with TtsHelper and WordChallengeGenerator.")

        viewModelScope.launch {
            _gameSettings.collect { settings ->
                Log.i("GameViewModel", "Game settings updated: $settings")
                playOrUpdateMusic(settings)
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
        lastPronouncedTarget = null
        Log.d("GameViewModel", "Loading new word challenge...")
        cancelAutoAdvanceTimer()

        incorrectCount = 0
        hintCount = 0

        val challenge = wordChallengeGenerator.getRandomInitialChallenge(recentTargetWords.toSet())
        challenge?.let {
            // Track this target word for repetition avoidance
            if (recentTargetWords.size >= 10) recentTargetWords.removeFirst()
            recentTargetWords.addLast(it.targetWord)
        }
        _currentChallenge.value = challenge
        _feedbackState.value = GameFeedback.None
        _disabledWords.value = emptySet()
        _hintLevel.value = 0
        _isHintButtonEnabled.value = true

        challenge?.let {
            if (_gameSettings.value.pronounceTargetAtStart) pronounceWord(it.targetWord)
            Log.i("GameViewModel", "New word loaded: ${it.targetWord}.")
        } ?: Log.w("GameViewModel", "No new word challenge could be loaded.")
    }

    fun processPlayerChoice(selectedWord: String) {
        // If previous answer was correct, just say the word and do nothing else
        if (_feedbackState.value is GameFeedback.Correct) {
            encouragementJob?.cancel()
            pronounceWord(selectedWord)
            return
        }
        val challenge = _currentChallenge.value
        Log.d("GameViewModel", "Processing player choice: $selectedWord, Challenge: $challenge")
        autoAdvanceJob?.cancel()
        _isTimerRunning.value = false

        if (challenge == null) {
            Log.w("GameViewModel", "No active challenge. Cannot process choice.")
            return
        }

        encouragementJob?.cancel()
        pronounceWord(selectedWord)
        val isCorrect = selectedWord == challenge.correctImageWord

        // Speak a feedback phrase ~1s after word
        encouragementJob = viewModelScope.launch {
            delay(1000L)
            val list = if (isCorrect) spokenContent.positive else spokenContent.encouragement
            val saying = if (list.isNotEmpty()) list.shuffled().first() else if (isCorrect) "Correct!" else "Try again!"
            pronounceWord(saying, pitch = if (isCorrect) 1.2f else 1.0f)
        }

        if (isCorrect) {
            _feedbackState.value = GameFeedback.Correct(selectedWord)
            _isHintButtonEnabled.value = false

            var pts = 10
            pts -= 3 * hintCount
            if (incorrectCount == 1) pts -= 3
            if (incorrectCount == 2) pts -= 8 // -3 first, -5 second
            if (pts < 1) pts = 1
            val newScore = _score.value + pts
            val newStreak = _streak.value + 1
            _score.value = newScore
            _streak.value = newStreak
            viewModelScope.launch { scoreStreakRepo.setScore(newScore) }
            viewModelScope.launch { scoreStreakRepo.setStreak(newStreak) }
            // Update high scores if needed
            if (newScore > _scoreHigh.value) {
                _scoreHigh.value = newScore
                viewModelScope.launch { scoreStreakRepo.setScoreHigh(newScore) }
            }
            if (newStreak > _streakHigh.value) {
                _streakHigh.value = newStreak
                viewModelScope.launch { scoreStreakRepo.setStreakHigh(newStreak) }
            }
            _streakDelta.value = 1 // Always +1 for correct answers
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _streakDelta.value = null
            }
            _disabledWords.value = emptySet() // Reset on correct
            if (_gameSettings.value.autoAdvance) {
                startAutoAdvanceTimer()
            } else {
                Log.i("GameViewModel", "Correct answer. Auto-advance is off. Waiting for manual next.")
            }
        } else {
            _feedbackState.value = GameFeedback.Incorrect(selectedWord)
            val previousStreak = _streak.value
            _streak.value = 0
            _score.value = 0
            viewModelScope.launch { scoreStreakRepo.setStreak(0) }
            viewModelScope.launch { scoreStreakRepo.setScore(0) }
            // Show negative streak delta (lost streak)
            if (previousStreak > 0) {
                _streakDelta.value = -previousStreak
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _streakDelta.value = null
                }
            }
            // Add this word to the set of disabled words
            _disabledWords.value = _disabledWords.value + selectedWord
            incorrectCount++
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

    fun pauseAutoAdvanceTimer() {
        autoAdvanceJob?.cancel()
        _isTimerRunning.value = false
        Log.i("GameViewModel", "Auto-advance timer paused due to background or lifecycle event.")
    }

    // This function is now more about updating the _gameSettings StateFlow.
    // The collectLatest block on _gameSettings will handle reacting to the change.
    fun updateSettings(newSettings: GameSettings) {
        val oldSettings = _gameSettings.value
        var persistSettings = newSettings
        // Only persist the interval if user changes it or if auto-advance is enabled.
        // If toggling auto-advance OFF, never overwrite or clear persisted interval value.
        // If toggling ON or changing interval, always persist and enforce minimum.
        val minDelay = 3
        val lastInterval = oldSettings.autoAdvanceIntervalSeconds

        val clampedInterval = if (newSettings.autoAdvanceIntervalSeconds < minDelay) minDelay else newSettings.autoAdvanceIntervalSeconds
        persistSettings = if (!oldSettings.autoAdvance && newSettings.autoAdvance) {
            newSettings.copy(autoAdvanceIntervalSeconds = if (clampedInterval < minDelay) minDelay else clampedInterval)
        } else if (!newSettings.autoAdvance) {
            // When toggling OFF, retain last interval from oldSettings
            newSettings.copy(autoAdvanceIntervalSeconds = lastInterval)
        } else {
            // Normal case: interval must still be clamped
            newSettings.copy(autoAdvanceIntervalSeconds = clampedInterval)
        }
        viewModelScope.launch {
            gameSettingsRepo.setSettings(persistSettings)
        }
        Log.i("GameViewModel", "ViewModel: Updating game settings to: $persistSettings")
        _gameSettings.value = persistSettings

        if (oldSettings.autoAdvance && !persistSettings.autoAdvance && _isTimerRunning.value) {
            cancelAutoAdvanceTimer()
            Log.i("GameViewModel", "Auto-advance turned off during a running timer. Timer cancelled.")
        }
        // All future state change triggers remain the same
    }

    fun pronounceWord(word: String, pitch: Float? = null, rate: Float? = null, asTargetWord: Boolean = false) {
        if (!_gameSettings.value.ttsEnabled) return
        if (asTargetWord) {
            spellWordForTts(word, pitch = pitch, rate = rate)
            return
        }

        encouragementJob?.cancel()
        spellJob?.cancel()
        if (isTtsReady.value) {
            val spoken = when {
                word.length > 1 && word.uppercase() == word -> word.lowercase()
                else -> word
            }
            val effectiveRate = rate ?: _gameSettings.value.ttsSpeed
            ttsHelper.speak(spoken, pitch = pitch, rate = effectiveRate)
        } else {
            Log.w("GameViewModel", "Attempted to pronounceWord, but TTS is not ready. Word: $word")
        }
    }

    private var spellJob: Job? = null

    fun spellWordForTts(word: String, pitch: Float? = null, rate: Float? = null) {
        spellJob?.cancel()
        spellJob = viewModelScope.launch {
            if (isTtsReady.value) {
                val currentSettings = gameSettings.value
                val effectiveRate = rate ?: currentSettings.ttsSpeed
                for ((i, c) in word.withIndex()) {
                    ttsHelper.speak(c.uppercaseChar().toString(), pitch = pitch, rate = effectiveRate)
                    if (i < word.lastIndex) delay(currentSettings.letterSpellingDelayMs.toLong())
                }
            } else {
                Log.w("GameViewModel", "Attempted to spell word, but TTS is not ready. Word: $word")
            }
        }
    }

    // Call this if the player manually chooses to go to the next word
    // (e.g. presses a "Next Word" button when auto-advance is off or they want to skip the timer)
    fun requestNextWordManually() {
        Log.i("GameViewModel", "Manual request for next word.")
        cancelAutoAdvanceTimer() // Stop any current auto-advance timer
        loadNewWordChallenge()
    }

    // --- Background Music Player ---
    private var bgMusicPlayer: android.media.MediaPlayer? = null
    private var lastLoadedTrack: String? = null

    fun pauseBackgroundMusic() {
        try { bgMusicPlayer?.pause() } catch (_: Exception) {}
    }

    fun resumeBackgroundMusic() {
        try { if (bgMusicPlayer?.isPlaying == false) bgMusicPlayer?.start() } catch (_: Exception) {}
    }

    private fun ensureMusicStoppedAndReleased() {
        try { bgMusicPlayer?.stop() } catch (_: Exception) {}
        try { bgMusicPlayer?.release() } catch (_: Exception) {}
        bgMusicPlayer = null
    }

    private fun playOrUpdateMusic(settings: GameSettings) {
        val volume = (settings.musicVolume.coerceIn(0, 100) / 200f) // UI 0-100 -> 0-0.5
        val musicRes = getRawResIdForTrack(settings.bgMusicTrack)
        if (settings.musicVolume == 0 || musicRes == 0) {
            ensureMusicStoppedAndReleased()
            return
        }
        if (bgMusicPlayer == null || lastLoadedTrack != settings.bgMusicTrack) {
            ensureMusicStoppedAndReleased()
            if (musicRes != 0) {
                bgMusicPlayer = android.media.MediaPlayer.create(appContext, musicRes)
                bgMusicPlayer?.isLooping = true
                lastLoadedTrack = settings.bgMusicTrack
            }
        }
        try { bgMusicPlayer?.setVolume(volume, volume) } catch (_: Exception) {}
        if (bgMusicPlayer?.isPlaying != true) {
            try { bgMusicPlayer?.start() } catch (_: Exception) {}
        }
    }

    private fun getRawResIdForTrack(track: String): Int {
        val resName = track.removeSuffix(".mp3").removeSuffix(".wav")
        return appContext.resources.getIdentifier(resName, "raw", appContext.packageName)
    }

    override fun onCleared() {
        super.onCleared()
        ensureMusicStoppedAndReleased()
        autoAdvanceJob?.cancel() // Ensure timer is cancelled when ViewModel is cleared
        encouragementJob?.cancel()
        Log.i("GameViewModel", "GameViewModel cleared.")
    }

    fun requestHint() {
        val currentLevel = _hintLevel.value
        val maxLevel = _gameSettings.value.hintLevelAllowed
        if (currentLevel < maxLevel) {
            _hintLevel.value = currentLevel + 1
            _isHintButtonEnabled.value = _hintLevel.value < maxLevel
            hintCount++
            if (currentLevel == 0 && _currentChallenge.value?.targetWord != null) {
                spellWordForTts(_currentChallenge.value!!.targetWord)
            }
            if (_hintLevel.value == 2) {
                // Tier 2: Eliminate one incorrect image (pick a word to disable)
                val challenge = _currentChallenge.value
                if (challenge != null) {
                    val incorrectWords = listOf(challenge.incorrectImageWord1, challenge.incorrectImageWord2)
                    // Only add one word that's not already disabled
                    val notYetDisabled = incorrectWords.filter { it !in _disabledWords.value }
                    if (notYetDisabled.isNotEmpty()) {
                        val disabledWord = notYetDisabled.first()
                        _disabledWords.value = _disabledWords.value + disabledWord
                        pronounceWord(disabledWord)
                    }
                }
            }
        } else {
            _isHintButtonEnabled.value = false
        }
    }

    // Load spoken_content.json from assets for TTS feedback
    private fun loadSpokenContent() {
        try {
            val inputStream = appContext.assets.open("spoken_content.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            spokenContent = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        } catch (ioException: IOException) {
            Log.e("GameViewModel", "Error reading spoken_content.json", ioException)
            spokenContent = SpokenContent()
        } catch (e: Exception) {
            Log.e("GameViewModel", "Error parsing spoken_content.json", e)
            spokenContent = SpokenContent()
        }
    }

    fun resetGame() {
        _score.value = 0
        _streak.value = 0
        _scoreHigh.value = 0
        _streakHigh.value = 0
        viewModelScope.launch { scoreStreakRepo.reset() }
        _hintLevel.value = 0
        _isHintButtonEnabled.value = true
        _feedbackState.value = GameFeedback.None
        _disabledWords.value = emptySet()
        cancelAutoAdvanceTimer()
        loadNewWordChallenge()
    }

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
    val hintLevelAllowed: Int = 2, // e.g., 0=none, 1=highlight, 2=show word
    val alwaysShowWords: Boolean = false,
    val pronounceTargetAtStart: Boolean = false,
    val musicVolume: Int = 100, // 0-100 (UI; 0=off)
    val ttsVolume: Int = 100, // 0-100 (UI)
    val bgMusicTrack: String = "chill.mp3",
    val ttsEnabled: Boolean = true,
    val letterSpellingDelayMs: Int = 750 // 500-2000ms in 250ms increments
)
