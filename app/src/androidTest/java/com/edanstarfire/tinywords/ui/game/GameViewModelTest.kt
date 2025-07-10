package com.edanstarfire.tinywords.ui.game

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.edanstarfire.tinywords.WordChallenge
import com.edanstarfire.tinywords.WordChallengeGenerator
import com.edanstarfire.tinywords.WordDefinition
import com.edanstarfire.tinywords.tts.TtsHelper
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class GameViewModelTest {
    private lateinit var context: Context
    private lateinit var ttsHelper: TtsHelper
    private lateinit var generator: WordChallengeGenerator
    private lateinit var viewModel: GameViewModel

    private val challenge = WordChallenge(
        targetWord = "CAT",
        correctImageWord = "CAT",
        correctImageRes = 1,
        incorrectImageWord1 = "BAT",
        incorrectImageRes1 = 2,
        incorrectImageWord2 = "HAT",
        incorrectImageRes2 = 3
    )

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ttsHelper = TtsHelper(context)
        val wordList = listOf(
            WordDefinition("CAT", "cat_image", "C", "A", "T"), // Assuming 'cat_image' is a real drawable name
            WordDefinition("BAT", "bat_image", "B", "A", "T"),
            WordDefinition("HAT", "hat_image", "H", "A", "T")
        )
        generator = WordChallengeGenerator(context, wordList, deterministic = true)
        viewModel = GameViewModel(ttsHelper, generator)
    }

    @Test
    fun loadNewWordChallenge_setsCurrentChallengeAndFeedbackState() {
        viewModel.loadNewWordChallenge()
        assertNotNull(viewModel.currentChallenge.value)
        assertEquals(GameFeedback.None, viewModel.feedbackState.value)
    }

    @Test
    fun processPlayerChoice_correctAnswer_updatesScoreFeedbackAndStreak() {
        viewModel.loadNewWordChallenge()
        viewModel.processPlayerChoice("CAT")
        assertEquals("CAT", viewModel.currentChallenge.value?.targetWord)
        assertEquals(GameFeedback.Correct("CAT"), viewModel.feedbackState.value)
        assertEquals(10, viewModel.score.value)
        assertEquals(1, viewModel.streak.value)
    }

    @Test
    fun processPlayerChoice_incorrectAnswer_updatesFeedbackAndResetsStreak() {
        viewModel.loadNewWordChallenge()
        viewModel.processPlayerChoice("DOG")
        assertEquals(0, viewModel.score.value)
        assertEquals(0, viewModel.streak.value)
        assertEquals(GameFeedback.Incorrect("DOG"), viewModel.feedbackState.value)
    }

    @Test
    fun resetGame_setsScoreAndStateToInitial() {
        viewModel.loadNewWordChallenge()
        viewModel.processPlayerChoice("CAT")
        viewModel.resetGame()
        assertEquals(0, viewModel.score.value)
        assertEquals(0, viewModel.streak.value)
        assertEquals(GameFeedback.None, viewModel.feedbackState.value)
        assertNotNull(viewModel.currentChallenge.value)
    }

    @After
    fun tearDown() {

    }
}