package com.edanstarfire.tinywords

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edanstarfire.tinywords.ui.game.GameSettings
import com.edanstarfire.tinywords.ui.game.GameViewModel
import com.edanstarfire.tinywords.WordChallengeGenerator
import com.edanstarfire.tinywords.tts.TtsHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import android.content.Context

@RunWith(AndroidJUnit4::class)
class GameViewModelTest {
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val ttsHelper = TtsHelper(context)
        val wordList = emptyList<com.edanstarfire.tinywords.WordDefinition>()
        val generator = WordChallengeGenerator(context, wordList, deterministic = true)
        viewModel = GameViewModel(ttsHelper, generator, context)
    }

    @Test
    fun autoAdvanceInterval_togglePersistence() {
        val initial = GameSettings(autoAdvance = true, autoAdvanceIntervalSeconds = 10)
        viewModel.updateSettings(initial)
        assertEquals(10, viewModel.gameSettings.value.autoAdvanceIntervalSeconds)
        assertEquals(true, viewModel.gameSettings.value.autoAdvance)

        // Toggle off, should not clear interval
        viewModel.updateSettings(initial.copy(autoAdvance = false))
        assertEquals(false, viewModel.gameSettings.value.autoAdvance)
        assertEquals(10, viewModel.gameSettings.value.autoAdvanceIntervalSeconds)

        // Toggle back on, should still be 10 (not 3, not default, not zero)
        viewModel.updateSettings(initial.copy(autoAdvance = true))
        assertEquals(true, viewModel.gameSettings.value.autoAdvance)
        assertEquals(10, viewModel.gameSettings.value.autoAdvanceIntervalSeconds)

        // Set to invalid low value and toggle
        viewModel.updateSettings(initial.copy(autoAdvanceIntervalSeconds = 1, autoAdvance = false))
        // Interval should remain the previous value (10) ON toggle off
        assertEquals(10, viewModel.gameSettings.value.autoAdvanceIntervalSeconds)
        
        // Now toggle back ON, interval should clamp to min 3
        viewModel.updateSettings(initial.copy(autoAdvanceIntervalSeconds = 1, autoAdvance = true))
        assertEquals(3, viewModel.gameSettings.value.autoAdvanceIntervalSeconds)
    }
}
