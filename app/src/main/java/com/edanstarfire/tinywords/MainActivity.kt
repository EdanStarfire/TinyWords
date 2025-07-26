package com.edanstarfire.tinywords

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TinyWordsTheme(darkTheme = false, dynamicColor = false) {
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
fun ScoreProgressBar(
    score: Int,
    highScore: Int,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = when {
        highScore <= 0 -> 1f
        score >= highScore -> 1f
        else -> score.toFloat() / highScore
    }
    val progressBgColor = Color(0xFFF8EAF6) // Light pastel
    val progressFillColor = Color(0xFFFF69B4) // Pink (Hot Pink)
    val rainbowColors = listOf(
        Color(0xFFFF1744), // Red
        Color(0xFFFFEA00), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF2979FF), // Blue
        Color(0xFFD500F9), // Violet
    )
    val rainbowBrushV = androidx.compose.ui.graphics.Brush.linearGradient(colors = rainbowColors, start = androidx.compose.ui.geometry.Offset(0f,0f), end = androidx.compose.ui.geometry.Offset(0f,1000f))
    val rainbowBrushH = androidx.compose.ui.graphics.Brush.linearGradient(colors = rainbowColors.reversed(), start = androidx.compose.ui.geometry.Offset(0f,0f), end = androidx.compose.ui.geometry.Offset(1000f,0f))

    if (isLandscape) {
        Box(
            modifier = modifier
                .fillMaxHeight(0.85f)
                .then(androidx.compose.ui.Modifier.width(32.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                // Draw light background
                drawRoundRect(
                    color = progressBgColor,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx())
                )
                // Draw filled progress (vertical fill, bottom up)
                if (progress > 0f) {
                    drawRoundRect(
                        color = progressFillColor,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * (1f - progress)),
                        size = androidx.compose.ui.geometry.Size(size.width, size.height * progress),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx())
                    )
                }
                // Draw rainbow border stroke
                drawRoundRect(
                    brush = rainbowBrushV,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
            }
            Text(
                text = "%d".format(score),
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(top = 8.dp)
            )
        }
    } else {
        Box(
            modifier = modifier
                .then(androidx.compose.ui.Modifier.fillMaxWidth(0.9f))
                .then(androidx.compose.ui.Modifier.height(30.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                // Draw light background
                drawRoundRect(
                    color = progressBgColor,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx())
                )
                // Draw filled progress (horizontal fill, left to right)
                if (progress > 0f) {
                    drawRoundRect(
                        color = progressFillColor,
                        size = androidx.compose.ui.geometry.Size(size.width * progress, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx())
                    )
                }
                // Draw rainbow border stroke
                drawRoundRect(
                    brush = rainbowBrushH,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
            }
            Text(
                text = "%d".format(score),
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterStart).padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier, viewModel: GameViewModel?) {
    var settingsDialogOpen by remember { mutableStateOf(false) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val bgRes = if (isLandscape) R.drawable.background_landscape else R.drawable.background_portait
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = bgRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop
    )
    if (isLandscape) {
        val score by viewModel?.score?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
        val scoreHigh by viewModel?.scoreHigh?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
        androidx.compose.foundation.layout.Row(
            modifier = modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Left: Score only
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxHeight().weight(2f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                ScoreProgressBar(score = score, highScore = scoreHigh, isLandscape = true, modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
            // Center: Target above choices
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxHeight().weight(6f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                TargetWordArea(viewModel)
                if (!isLandscape) Spacer(modifier = Modifier.height(24.dp))
                ImageChoicesArea(viewModel, isLandscape = isLandscape)
            }
            // Right: Options/buttons column
            GameBorder(viewModel) { settingsDialogOpen = it }
        }
    } else {
        // Portrait/Default
        val score by viewModel?.score?.collectAsState()
            ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
        val scoreHigh by viewModel?.scoreHigh?.collectAsState()
            ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
        androidx.compose.foundation.layout.Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Add progress bar at the top in portrait
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                ScoreProgressBar(score = score, highScore = scoreHigh, isLandscape = false)
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.then(Modifier.weight(2.3f)).fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                TargetWordArea(viewModel)
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                ImageChoicesArea(viewModel, isLandscape = false)
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxHeight().weight(1.7f)
            ) {
                GameBorder(viewModel) { settingsDialogOpen = it }
            }
        }
    }
    if (settingsDialogOpen && viewModel != null) {
        ThemedSettingsModal(
            onDismiss = { settingsDialogOpen = false },
            content = {
                SettingsDialogContent(
                    currentSettings = viewModel.gameSettings.collectAsState().value,
                    onSettingsChange = { viewModel.updateSettings(it) },
                    onResetGame = { viewModel.resetGame() },
                    onDismiss = { settingsDialogOpen = false }
                )
            }
        )
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
                    fontSize = 115.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier
                        .padding(if (androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 0.dp else 16.dp)
                        .clickable { viewModel.pronounceWord(target, asTargetWord = true) }
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
    val density = androidx.compose.ui.platform.LocalDensity.current
    val borderRadiusPx = with(density) { 20.dp.toPx() }
    val borderWidthPx = with(density) { 5.dp.toPx() }
    val borderColor = when {
        isSelected && isCorrect -> androidx.compose.ui.graphics.Color(0xFF388E3C)
        isSelected && !isCorrect -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
        else -> androidx.compose.ui.graphics.Color.LightGray
    }
    // --- Animation: Shake on new incorrect tap ---
    var shakeTrigger by remember { mutableStateOf(0) }
    val shakeOffset = 12
    val animatedShake = animateFloatAsState(
        targetValue = shakeTrigger.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "Shake"
    )
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(5.dp, borderColor),
        tonalElevation = 2.dp,
        onClick = if (enabled && !isDisabled) {
            {
                shakeTrigger += 1
                onClick()
            }
        } else ({}),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .alpha(alpha)
            .graphicsLayer {
                if (animatedShake.value > shakeTrigger - 1) {
                    translationX = (kotlin.math.sin(animatedShake.value * 6 * Math.PI) * shakeOffset).toFloat()
                }
            }
            .drawWithContent {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawRoundRect(
                    color = borderColor,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx, borderRadiusPx),
                    style = Stroke(width = borderWidthPx, pathEffect = pathEffect)
                )
                drawContent()
            },
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                    .fillMaxWidth()
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
                    Text(text = word, fontSize = 35.sp)
                    Spacer(modifier = Modifier.height(6.dp))
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
                fontSize = 35.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ImageChoicesArea(viewModel: GameViewModel?, isLandscape: Boolean = false) {
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
            // 2x1 layout: first two centered across, third below
            if (isLandscape) {
        val score by viewModel?.score?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
        val scoreHigh by viewModel?.scoreHigh?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    data class ImageChoiceData(val word: String, @androidx.annotation.DrawableRes val res: Int)
                    val all = listOf(
                        ImageChoiceData(currentChallenge.correctImageWord, currentChallenge.correctImageRes),
                        ImageChoiceData(currentChallenge.incorrectImageWord1, currentChallenge.incorrectImageRes1),
                        ImageChoiceData(currentChallenge.incorrectImageWord2, currentChallenge.incorrectImageRes2)
                    )
                    val items = currentChallenge.choiceOrder.map { all[it] }
                    for (item in items) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.then(Modifier.weight(1f)).padding(8.dp)
                        ) {
                            ImageChoice(
                                word = item.word,
                                res = item.res,
                                enabled = true,
                                isCorrect = item.word == currentChallenge.correctImageWord,
                                isSelected = (chosenWord == item.word) || (disabledWords.contains(item.word) && item.word != currentChallenge.correctImageWord && feedbackState !is com.edanstarfire.tinywords.ui.game.GameFeedback.None),
                                isDisabled = if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) false else disabledWords.contains(item.word) && chosenWord != currentChallenge.correctImageWord,
                                showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                    (item.word in disabledWords) ||
                                    (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct),
                                differingIndex = if (
                                    feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct ||
                                    (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect && ((feedbackState as com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect).chosenWord == item.word)) ||
                                    (viewModel.hintLevel.collectAsState().value == 2 && viewModel.disabledWords.collectAsState().value.contains(item.word))
                                ) {
                                    val tgt = currentChallenge.targetWord
                                    val incorrect1 = currentChallenge.incorrectImageWord1
                                    val incorrect2 = currentChallenge.incorrectImageWord2
                                    tgt.indices.firstOrNull { idx ->
                                        (idx < incorrect1.length && tgt[idx] != incorrect1[idx]) ||
                                                (idx < incorrect2.length && tgt[idx] != incorrect2[idx])
                                    }
                                } else null,
                                onClick = { viewModel.processPlayerChoice(item.word) }
                            )
                        }
                    }
                }
            } else androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                data class ImageChoiceData(
                    val word: String,
                    @androidx.annotation.DrawableRes val res: Int
                )

                val all = listOf(
                    ImageChoiceData(
                        currentChallenge.correctImageWord,
                        currentChallenge.correctImageRes
                    ),
                    ImageChoiceData(
                        currentChallenge.incorrectImageWord1,
                        currentChallenge.incorrectImageRes1
                    ),
                    ImageChoiceData(
                        currentChallenge.incorrectImageWord2,
                        currentChallenge.incorrectImageRes2
                    )
                )
                val items = currentChallenge.choiceOrder.map { all[it] }
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    for (i in 0..1) {
                        if (i < items.size) {
                            val item = items[i]
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.then(Modifier.weight(1f)).padding(8.dp)
                            ) {
                                ImageChoice(
                                    word = item.word,
                                    res = item.res,
                                    enabled = true,
                                    isCorrect = item.word == currentChallenge.correctImageWord,
                                    isSelected = (chosenWord == item.word) || (disabledWords.contains(
                                        item.word
                                    ) && item.word != currentChallenge.correctImageWord && feedbackState !is com.edanstarfire.tinywords.ui.game.GameFeedback.None),
                                    isDisabled = if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) false else disabledWords.contains(
                                        item.word
                                    ) && chosenWord != currentChallenge.correctImageWord,
                                    showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                            (item.word in disabledWords) ||
                                            (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct),
                                    differingIndex = if (
                                        feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct ||
                                        (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect && ((feedbackState as com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect).chosenWord == item.word)) ||
                                        (viewModel.hintLevel.collectAsState().value == 2 && viewModel.disabledWords.collectAsState().value.contains(
                                            item.word
                                        ))
                                    ) {
                                        val tgt = currentChallenge.targetWord
                                        val incorrect1 = currentChallenge.incorrectImageWord1
                                        val incorrect2 = currentChallenge.incorrectImageWord2
                                        tgt.indices.firstOrNull { idx ->
                                            (idx < incorrect1.length && tgt[idx] != incorrect1[idx]) ||
                                                    (idx < incorrect2.length && tgt[idx] != incorrect2[idx])
                                        }
                                    } else null,
                                    onClick = { viewModel.processPlayerChoice(item.word) }
                                )
                            }
                        }
                    }
                }
                if (items.size > 2) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(0.5f))
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.then(Modifier.weight(1f))
                                .padding(top = 24.dp, start = 8.dp, end = 8.dp)
                        ) {
                            val item = items[2]
                            ImageChoice(
                                word = item.word,
                                res = item.res,
                                enabled = true,
                                isCorrect = item.word == currentChallenge.correctImageWord,
                                isSelected = (chosenWord == item.word) || (disabledWords.contains(
                                    item.word
                                ) && item.word != currentChallenge.correctImageWord && feedbackState !is com.edanstarfire.tinywords.ui.game.GameFeedback.None),
                                isDisabled = if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) false else disabledWords.contains(
                                    item.word
                                ) && chosenWord != currentChallenge.correctImageWord,
                                showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                        (item.word in disabledWords) ||
                                        (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct),
                                differingIndex = if (
                                    feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct ||
                                    (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect && ((feedbackState as com.edanstarfire.tinywords.ui.game.GameFeedback.Incorrect).chosenWord == item.word)) ||
                                    (viewModel.hintLevel.collectAsState().value == 2 && viewModel.disabledWords.collectAsState().value.contains(
                                        item.word
                                    ))
                                ) {
                                    val tgt = currentChallenge.targetWord
                                    val incorrect1 = currentChallenge.incorrectImageWord1
                                    val incorrect2 = currentChallenge.incorrectImageWord2
                                    tgt.indices.firstOrNull { idx ->
                                        (idx < incorrect1.length && tgt[idx] != incorrect1[idx]) ||
                                                (idx < incorrect2.length && tgt[idx] != incorrect2[idx])
                                    }
                                } else null,
                                onClick = { viewModel.processPlayerChoice(item.word) }
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(0.5f))

                    }
                }
            }
        }
    } else {
        Text(text = "ImageChoicesArea", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SettingsDialogContent(
    currentSettings: com.edanstarfire.tinywords.ui.game.GameSettings,
    onSettingsChange: (com.edanstarfire.tinywords.ui.game.GameSettings) -> Unit,
    onResetGame: () -> Unit,
    onDismiss: () -> Unit
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

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(28.dp)
            .fillMaxWidth()
            .heightIn(max = 520.dp) // prevents infinite height crash
    ) {
        androidx.compose.material3.Text(
            text = stringResource(id = R.string.settings_title),
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text(text = stringResource(id = R.string.button_cancel))
            }
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
        }
    }
}


@Composable
fun NextButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseNextBtn")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "PulseNextBtn"
    )
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = R.drawable.btn_next),
        contentDescription = "Next Word",
        modifier = modifier
            .size(80.dp)
            .graphicsLayer { scaleX = pulse; scaleY = pulse }
            .clickable { onClick() }
    )
}

@Composable
fun GameBorder(viewModel: GameViewModel?, onDialogOpenChange: (Boolean) -> Unit) {
    if (viewModel != null) {
        val streak by viewModel.streak.collectAsState()
        val score by viewModel.score.collectAsState()
        val scoreDelta by viewModel.scoreDelta.collectAsState()
        val feedbackState by viewModel.feedbackState.collectAsState()
        val isHintEnabled by viewModel.isHintButtonEnabled.collectAsState()
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        if (!isLandscape) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {
                // +N popup above the score
                androidx.compose.animation.AnimatedVisibility(
                    visible = (scoreDelta ?: 0) > 0,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(start = 56.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "+${scoreDelta ?: 0}",
                        fontSize = 22.sp,
                        color = androidx.compose.ui.graphics.Color(0xFF388E3C),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                    )
                }


                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.btn_hint),
                        contentDescription = "Help",
                        modifier = Modifier
                            .size(60.dp)
                            .clickable(enabled = isHintEnabled) { viewModel.requestHint() }
                            .alpha(if (isHintEnabled) 1f else 0.4f)
                    )
                    Spacer(modifier = Modifier.then(Modifier.weight(1f)))
                    if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) {
                        NextButton(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)) { viewModel.requestNextWordManually() }
                    } else {
                        Spacer(modifier = Modifier.size(80.dp))
                    }
                    Spacer(modifier = Modifier.then(Modifier.weight(1f)))
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.btn_options),
                        contentDescription = stringResource(id = R.string.button_options),
                        modifier = Modifier
                            .size(60.dp)
                            .clickable {
                                viewModel.cancelAutoAdvanceTimer()
                                onDialogOpenChange(true)
                            }
                    )
                }
            }
        } else {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
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

                // Vertical options/buttons only
                if (viewModel != null) {
                    val feedbackState by viewModel.feedbackState.collectAsState()
                    val isHintEnabled by viewModel.isHintButtonEnabled.collectAsState()
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterEnd)
                            .fillMaxHeight(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        horizontalAlignment = androidx.compose.ui.Alignment.End
                    ) {
                        // Options Button top
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.btn_options),
                            contentDescription = stringResource(id = R.string.button_options),
                            modifier = Modifier
                                .size(60.dp)
                                .clickable {
                                    viewModel.cancelAutoAdvanceTimer()
                                    onDialogOpenChange(true)
                                }
                        )
                        // Next Button, only if correct
                        if (feedbackState is com.edanstarfire.tinywords.ui.game.GameFeedback.Correct) {
                            NextButton(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)) { viewModel.requestNextWordManually() }
                        } else {
                            Spacer(modifier = Modifier.size(80.dp))
                        }
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.btn_hint),
                            contentDescription = "Help",
                            modifier = Modifier
                                .size(60.dp)
                                .clickable(enabled = isHintEnabled) { viewModel.requestHint() }
                                .alpha(if (isHintEnabled) 1f else 0.4f)
                        )
                    }
                }
            }
        }
    } else {
        androidx.compose.material3.Text(text = "GameBorder", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun ThemedSettingsModal(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val borderRadiusPx = with(density) { 32.dp.toPx() }
    val borderWidthPx = with(density) { 7.dp.toPx() }
    val dash = 16f
    val gap = 10f
    val rainbow = listOf(
        Color.Red, Color(0xFFFD9A04), Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
    )
    fun Modifier.rainbowDashedBorder() = this.then(
        Modifier.drawWithContent {
            val sweep = androidx.compose.ui.graphics.Brush.sweepGradient(rainbow)
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
            drawRoundRect(
                brush = sweep,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(borderRadiusPx, borderRadiusPx),
                style = Stroke(width = borderWidthPx, pathEffect = pathEffect)
            )
            drawContent()
        }
    )
    AnimatedVisibility(
        visible = true,
        enter = androidx.compose.animation.fadeIn(tween(210)) +
            androidx.compose.animation.slideInVertically(tween(350), initialOffsetY = { it / 4 }),
        exit = androidx.compose.animation.fadeOut(tween(210)) +
            androidx.compose.animation.slideOutVertically(tween(210), targetOffsetY = { it / 4 })
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
                .clickable(onClick = onDismiss, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() })
        ) {
            Box(
                modifier = Modifier
                    .padding(32.dp)
                    .rainbowDashedBorder()
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .align(androidx.compose.ui.Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {}
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    TinyWordsTheme(darkTheme = false, dynamicColor = false) {
        GameScreen(viewModel = null)
    }
}