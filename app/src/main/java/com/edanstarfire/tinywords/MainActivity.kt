package com.edanstarfire.tinywords

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.edanstarfire.tinywords.ui.theme.TinyWordsTheme
import dagger.hilt.android.AndroidEntryPoint
import com.edanstarfire.tinywords.ui.game.GameViewModel
import androidx.compose.runtime.collectAsState  // <-- Import this
import androidx.compose.runtime.getValue      // <-- Import this
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.verticalScroll

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TinyWordsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = gameViewModel
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.pauseAutoAdvanceTimer()
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier, viewModel: GameViewModel?) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Arrange areas horizontally
        androidx.compose.foundation.layout.Row(
            modifier = modifier.fillMaxSize().padding(8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(3f).fillMaxHeight(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                TargetWordArea(viewModel)
            }
            androidx.compose.material3.VerticalDivider()
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(5f).fillMaxHeight(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                ImageChoicesArea(viewModel)
            }
            androidx.compose.material3.VerticalDivider()
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(2f).fillMaxHeight(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                GameBorder(viewModel)
            }
        }
        return
    }
    // Portrait/Default
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(2f).fillMaxWidth(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            TargetWordArea(viewModel)
        }
        androidx.compose.material3.HorizontalDivider()
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(6f).fillMaxWidth(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            ImageChoicesArea(viewModel)
        }
        androidx.compose.material3.HorizontalDivider()
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(2f).fillMaxWidth()
        ) {
            GameBorder(viewModel)
        }
    }
}

@Composable
fun TargetWordArea(viewModel: GameViewModel?) {
    // Show target word large, centered from currentChallenge, with Tier 1 highlighting & TTS
    if (viewModel != null) {
        val currentChallenge by viewModel.currentChallenge.collectAsState()
        val hintLevel by viewModel.hintLevel.collectAsState()
        val gameSettings by viewModel.gameSettings.collectAsState()
        val target = currentChallenge?.targetWord
        val incorrect1 = currentChallenge?.incorrectImageWord1
        val incorrect2 = currentChallenge?.incorrectImageWord2
        val differingIndex = if (hintLevel >= 1 && target != null && incorrect1 != null && incorrect2 != null) {
            target.indices.firstOrNull { idx ->
                (idx < incorrect1.length && target[idx] != incorrect1[idx]) ||
                (idx < incorrect2.length && target[idx] != incorrect2[idx])
            }
        } else null

        androidx.compose.runtime.LaunchedEffect(target, gameSettings.pronounceTargetAtStart) {
            if (target != null && gameSettings.pronounceTargetAtStart) {
                viewModel.pronounceWord(target, asTargetWord = true)
            }
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (target != null) {
                androidx.compose.material3.Text(
                    text = buildAnnotatedString {
                        target.forEachIndexed { i, c ->
                            if (i == differingIndex) {
                                withStyle(androidx.compose.ui.text.SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF388E3C))) { append(c) }
                            } else append(c)
                        }
                    },
                    fontSize = 72.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { viewModel.pronounceWord(target) }
                )
            } else {
                androidx.compose.material3.Text(text = "…", modifier = Modifier.padding(16.dp))
            }
        }
    } else {
        androidx.compose.material3.Text(text = "TargetWordArea", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun ImageChoice(
    word: String,
    res: Int,
    enabled: Boolean,
    isCorrect: Boolean,
    isSelected: Boolean,
    isDisabled: Boolean,
    showWordBelow: Boolean = false,
    differingIndex: Int? = null,
    onClick: () -> Unit
) {
    val alpha = if (isDisabled) 0.4f else 1f
    val borderColor = when {
        isSelected && isCorrect -> androidx.compose.ui.graphics.Color(0xFF388E3C)
        isSelected && !isCorrect -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
        else -> androidx.compose.ui.graphics.Color.LightGray
    }
    // --- Animation: Shake on new incorrect tap ---
    val shakeOffset = if (isSelected && !isCorrect) 12 else 0
    val animatedShake = animateFloatAsState(
        targetValue = if (isSelected && !isCorrect) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    androidx.compose.material3.Surface(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .alpha(alpha)
            .graphicsLayer {
                if (isSelected && !isCorrect && animatedShake.value > 0f) {
                    translationX = (kotlin.math.sin(animatedShake.value * 6 * Math.PI) * shakeOffset).toFloat()
                }
            },
        tonalElevation = 2.dp,
        onClick = if (enabled && !isDisabled) onClick else ({}),
        border = androidx.compose.foundation.BorderStroke(3.dp, borderColor),
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(12.dp)
                    .aspectRatio(1f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                if (res != 0) {
                    val painter = androidx.compose.ui.res.painterResource(id = res)
                    Image(
                        painter = painter,
                        contentDescription = word,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = word, fontSize = 25.sp)
                }
                // --- Correct/checkmark/try again overlay icons ---
                if (isSelected && isCorrect) {
                    androidx.compose.animation.AnimatedVisibility(true) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.TopEnd
                        ) {
                            val checkMark = androidx.compose.ui.res.painterResource(id = com.edanstarfire.tinywords.R.drawable.correct_icon)
                            Image(
                                painter = checkMark,
                                contentDescription = "Correct",
                                modifier = Modifier.size(48.dp).padding(2.dp),
                                alpha = 0.95f
                            )
                        }
                    }
                } else if (isSelected && !isCorrect) {
                    androidx.compose.animation.AnimatedVisibility(true) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.TopEnd
                        ) {
                            val tryAgainIcon = androidx.compose.ui.res.painterResource(id = com.edanstarfire.tinywords.R.drawable.try_again_icon)
                            Image(
                                painter = tryAgainIcon,
                                contentDescription = "Try Again",
                                modifier = Modifier.size(44.dp).padding(2.dp),
                                alpha = 0.95f
                            )
                        }
                    }
                }
            }
            androidx.compose.material3.Text(
                text = if (showWordBelow) buildAnnotatedString {
                    word.forEachIndexed { i, c ->
                        if (i == differingIndex) {
                            withStyle(androidx.compose.ui.text.SpanStyle(color = if (isCorrect) androidx.compose.ui.graphics.Color(0xFF388E3C) else androidx.compose.ui.graphics.Color(0xFFD32F2F))) {
                                append(c)
                            }
                        } else append(c)
                    }
                } else buildAnnotatedString { append(" ") },
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ImageChoicesArea(viewModel: GameViewModel?) {
    if (viewModel != null) {
        val currentChallengeState by viewModel.currentChallenge.collectAsState()
        val currentChallenge = currentChallengeState
        val feedbackState by viewModel.feedbackState.collectAsState()
        val chosenWord: String? = when (feedbackState) {
            is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct -> (feedbackState as com.edanstarfire.tinywords.ui.game.GameFeedback.Correct).chosenWord
            is com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect -> (feedbackState as com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect).chosenWord
            else -> null
        }
        val disabledWords by viewModel.disabledWords.collectAsState()
        if (currentChallenge != null) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                // Avoid Triple usage; use a local data class for clarity
                data class ImageChoiceData(val word: String, @androidx.annotation.DrawableRes val res: Int)
                val all = listOf(
                    ImageChoiceData(currentChallenge.correctImageWord, currentChallenge.correctImageRes),
                    ImageChoiceData(currentChallenge.incorrectImageWord1, currentChallenge.incorrectImageRes1),
                    ImageChoiceData(currentChallenge.incorrectImageWord2, currentChallenge.incorrectImageRes2)
                )
                val items = currentChallenge.choiceOrder.map { all[it] } // use fixed order from challenge
                for (item in items) {
                    val word = item.word
                    val res = item.res
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        ImageChoice(
                            word = word,
                            res = res,
                            enabled = true,
                            isCorrect = word == currentChallenge.correctImageWord,
                            isSelected = (chosenWord == word) || (disabledWords.contains(word) && word != currentChallenge.correctImageWord && feedbackState !is com.edanstarfire.tinywords.ui.game.GameFeedback.None),
                            isDisabled = if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) false else disabledWords.contains(word) && chosenWord != currentChallenge.correctImageWord,
                            showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                (word in disabledWords) ||
                                (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct),
                            differingIndex = if (
                                feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct ||
                                (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect && ((feedbackState as com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect).chosenWord == word)) ||
                                (viewModel.hintLevel.collectAsState().value == 2 && viewModel.disabledWords.collectAsState().value.contains(word))
                            ) {
                                val tgt = currentChallenge.targetWord
                                val incorrect1 = currentChallenge.incorrectImageWord1
                                val incorrect2 = currentChallenge.incorrectImageWord2
                                tgt.indices.firstOrNull { idx ->
                                    (idx < incorrect1.length && tgt[idx] != incorrect1[idx]) ||
                                    (idx < incorrect2.length && tgt[idx] != incorrect2[idx])
                                }
                            } else null,
                            onClick = { viewModel.processPlayerChoice(word) }
                        )
                    }
                }
            }
        } else {
            Text(text = "…", modifier = Modifier.padding(16.dp))
        }
    } else {
        Text(text = "ImageChoicesArea", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SettingsDialog(
    currentSettings: com.edanstarfire.tinywords.ui.game.GameSettings,
    onDismiss: () -> Unit,
    onSettingsChange: (com.edanstarfire.tinywords.ui.game.GameSettings) -> Unit,
    onResetGame: () -> Unit
) {
    val autoAdvanceChoices = listOf(
        Pair(3, stringResource(id = R.string.setting_timer_3_seconds)),
        Pair(5, stringResource(id = R.string.setting_timer_5_seconds)),
        Pair(8, stringResource(id = R.string.setting_timer_8_seconds)),
        Pair(10, stringResource(id = R.string.setting_timer_10_seconds)),
        Pair(30, "30 Seconds")
    )
    var autoAdvanceEnabled by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentSettings.autoAdvance) }
    var autoAdvanceInterval by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentSettings.autoAdvanceIntervalSeconds) }
    var alwaysShowWords by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentSettings.alwaysShowWords) }
    // For completeness: tts speed & pronounce-at-start toggles (hidden if not exposed in UI)
    var pronounceTargetAtStart by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentSettings.pronounceTargetAtStart) }
    var ttsSpeed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentSettings.ttsSpeed) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text(text = stringResource(id = R.string.settings_title)) },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                androidx.compose.material3.Text(stringResource(id = R.string.setting_label_auto_advance), modifier = Modifier.padding(vertical = 4.dp))
                androidx.compose.material3.Switch(
                    checked = autoAdvanceEnabled,
                    onCheckedChange = { autoAdvanceEnabled = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (autoAdvanceEnabled) {
                    androidx.compose.material3.Text("Interval:", modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                    for (pair in autoAdvanceChoices) {
                        val (value, label) = pair
                        Row(modifier = Modifier.padding(bottom = 2.dp)) {
                            androidx.compose.material3.RadioButton(
                                selected = autoAdvanceInterval == value,
                                onClick = { autoAdvanceInterval = value }
                            )
                            androidx.compose.material3.Text(label)
                        }
                    }
                }
                androidx.compose.material3.Text(stringResource(id = R.string.setting_label_show_words), modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                androidx.compose.material3.Switch(
                    checked = alwaysShowWords,
                    onCheckedChange = { alwaysShowWords = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                androidx.compose.material3.Text("Pronounce Target Word at Start", modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                androidx.compose.material3.Switch(
                    checked = pronounceTargetAtStart,
                    onCheckedChange = { pronounceTargetAtStart = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val showResetDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                androidx.compose.material3.Button(
                    onClick = { showResetDialog.value = true },
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    androidx.compose.material3.Text(stringResource(id = R.string.button_restart))
                }
                if (showResetDialog.value) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showResetDialog.value = false },
                        title = { androidx.compose.material3.Text(stringResource(id = R.string.dialog_restart_title)) },
                        text = { androidx.compose.material3.Text(stringResource(id = R.string.dialog_restart_message)) },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                showResetDialog.value = false
                                onResetGame()
                            }) {
                                androidx.compose.material3.Text(stringResource(id = R.string.button_restart))
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showResetDialog.value = false }) {
                                androidx.compose.material3.Text(stringResource(id = R.string.button_cancel))
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val newSettings = currentSettings.copy(
                    autoAdvance = autoAdvanceEnabled,
                    autoAdvanceIntervalSeconds = if (autoAdvanceEnabled) autoAdvanceInterval else 0,
                    alwaysShowWords = alwaysShowWords,
                    pronounceTargetAtStart = pronounceTargetAtStart,
                    ttsSpeed = ttsSpeed
                )
                onSettingsChange(newSettings)
                onDismiss()
            }) { androidx.compose.material3.Text(text = stringResource(id = R.string.button_ok)) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text(text = stringResource(id = R.string.button_cancel))
            }
        }
    )
}

@Composable
fun GameBorder(viewModel: GameViewModel?) {
    if (viewModel != null) {
        val streak by viewModel.streak.collectAsState()
        val score by viewModel.score.collectAsState()
        val scoreDelta by viewModel.scoreDelta.collectAsState()
        val feedbackState by viewModel.feedbackState.collectAsState()
        val isHintEnabled by viewModel.isHintButtonEnabled.collectAsState()
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
        ) {
            // +N popup above the score
            androidx.compose.animation.AnimatedVisibility(
                visible = (scoreDelta ?: 0) > 0,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter).padding(start = 56.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "+${scoreDelta ?: 0}",
                    fontSize = 22.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF388E3C),
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
            }
            // Score (top-left)
            androidx.compose.material3.Text(
                text = "Score: $score",
                fontSize = 20.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart)
            )

            // Streak counter (just below score)
            androidx.compose.material3.Text(
                text = "Streak: $streak",
                fontSize = 18.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart).padding(top = 28.dp)
            )


            // Help button (bottom-center)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder_1),
                contentDescription = "Help",
                modifier = Modifier
                    .size(60.dp)
                    .align(androidx.compose.ui.Alignment.BottomStart)
                    .clickable(enabled = isHintEnabled) { viewModel.requestHint() }
                    .alpha(if (isHintEnabled) 1f else 0.4f)
            )

            // Options/settings button (bottom-end)
            var settingsDialogOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder_1),
                contentDescription = stringResource(id = R.string.button_options),
                modifier = Modifier
                    .size(60.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .clickable {
                        viewModel.cancelAutoAdvanceTimer()
                        settingsDialogOpen = true
                    }
            )
            if (settingsDialogOpen) {
                SettingsDialog(
                    currentSettings = viewModel.gameSettings.collectAsState().value,
                    onDismiss = { settingsDialogOpen = false },
                    onSettingsChange = { viewModel.updateSettings(it) },
                    onResetGame = { viewModel.resetGame() }
                )
            }

            // Next Word button or timer (center, after correct)
            if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) {
                val isTimerRunning by viewModel.isTimerRunning.collectAsState()
                val timerValue by viewModel.timerValueSeconds.collectAsState()
                androidx.compose.material3.Button(
                    onClick = { viewModel.requestNextWordManually() },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                ) {
                    val label = if (isTimerRunning && timerValue > 0) "Next ${timerValue}s" else "Next Word"
                    androidx.compose.material3.Text(label)
                }
            }
        }
    } else {
        androidx.compose.material3.Text(text = "GameBorder", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    TinyWordsTheme {
        GameScreen(viewModel = null)
    }
}