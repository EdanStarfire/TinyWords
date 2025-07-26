package com.edanstarfire.tinywords

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.edanstarfire.tinywords.ui.game.GameFeedback
import com.edanstarfire.tinywords.ui.game.GameSettings
import kotlin.math.roundToInt
import kotlin.math.sin

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
fun ConfirmButton(
    modifier: Modifier = Modifier,
    fillColor: Color,
    borderColors: List<Color>,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                colors = borderColors,
                start = Offset.Zero,
                end = Offset.Infinite
            )
        ),
        color = fillColor,
        modifier = modifier
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
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
    val rainbowBrushV = Brush.linearGradient(colors = rainbowColors, start = androidx.compose.ui.geometry.Offset(0f,0f), end = androidx.compose.ui.geometry.Offset(0f,1000f))
    val rainbowBrushH = Brush.linearGradient(colors = rainbowColors.reversed(), start = androidx.compose.ui.geometry.Offset(0f,0f), end = androidx.compose.ui.geometry.Offset(1000f,0f))

    if (isLandscape) {
        Box(
            modifier = modifier
                .fillMaxHeight(0.85f)
                .then(Modifier.width(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Draw light background
                drawRoundRect(
                    color = progressBgColor,
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx(),12.dp.toPx())
                )
                // Draw filled progress (vertical fill, bottom up)
                if (progress > 0f) {
                    drawRoundRect(
                        color = progressFillColor,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * (1f - progress)),
                        size = Size(size.width, size.height * progress),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx())
                    )
                }
                // Draw rainbow border stroke
                drawRoundRect(
                    brush = rainbowBrushV,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(),12.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            Text(
                text = "%d".format(score),
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.BottomCenter).padding(top = 8.dp)
            )
        }
    } else {
        Box(
            modifier = modifier
                .then(Modifier.fillMaxWidth(0.9f))
                .then(Modifier.height(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
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
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            Text(
                text = "%d".format(score),
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier, viewModel: GameViewModel?) {
    var settingsDialogOpen by rememberSaveable { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bgRes = if (isLandscape) R.drawable.background_landscape else R.drawable.background_portait
    Image(
        painter = painterResource(id = bgRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    if (isLandscape) {
        val score by viewModel?.score?.collectAsState() ?: remember { mutableStateOf(0) }
        val scoreHigh by viewModel?.scoreHigh?.collectAsState() ?: remember { mutableStateOf(0) }
        Row(
            modifier = modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Score only
            Box(
                modifier = Modifier.fillMaxHeight().weight(2f),
                contentAlignment = Alignment.Center
            ) {
                ScoreProgressBar(score = score, highScore = scoreHigh, isLandscape = true, modifier = Modifier.align(
                    Alignment.Center))
            }
            // Center: Target above choices
            Column(
                modifier = Modifier.fillMaxHeight().weight(6f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
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
            ?: remember { mutableStateOf(0) }
        val scoreHigh by viewModel?.scoreHigh?.collectAsState()
            ?: remember { mutableStateOf(0) }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Add progress bar at the top in portrait
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                ScoreProgressBar(score = score, highScore = scoreHigh, isLandscape = false)
            }
            Column(
                modifier = Modifier.then(Modifier.weight(2.3f)).fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                TargetWordArea(viewModel)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                ImageChoicesArea(viewModel, isLandscape = false)
            }
            Column(
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

        LaunchedEffect(target, gameSettings.pronounceTargetAtStart) {
            if (target != null && gameSettings.pronounceTargetAtStart) {
                viewModel.pronounceWord(target, asTargetWord = true)
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (target != null) {
                Text(
                    text = buildAnnotatedString {
                        target.forEachIndexed { i, c ->
                            if (i == differingIndex) {
                                withStyle(SpanStyle(color = Color(0xFF388E3C))) { append(c) }
                            } else append(c)
                        }
                    },
                    fontSize = 115.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 0.dp else 16.dp)
                        .clickable { viewModel.pronounceWord(target, asTargetWord = true) }
                )
            } else {
                Text(text = "…", modifier = Modifier.padding(16.dp))
            }
        }
    } else {
        Text(text = "TargetWordArea", modifier = Modifier.padding(16.dp))
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
    val density = LocalDensity.current
    val borderRadiusPx = with(density) { 20.dp.toPx() }
    val borderWidthPx = with(density) { 5.dp.toPx() }
    val borderColor = when {
        isSelected && isCorrect -> Color(0xFF388E3C)
        isSelected && !isCorrect -> Color(0xFFD32F2F)
        else -> Color.LightGray
    }
    // --- Animation: Shake on new incorrect tap ---
    var shakeTrigger by remember { mutableStateOf(0) }
    val shakeOffset = 12
    val animatedShake = animateFloatAsState(
        targetValue = shakeTrigger.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "Shake"
    )
    Surface(
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
                    translationX = (sin(animatedShake.value * 6 * Math.PI) * shakeOffset).toFloat()
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
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (res != 0) {
                    val painter = painterResource(id = res)
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
                    this@Column.AnimatedVisibility(true) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            val checkMark = painterResource(id = R.drawable.correct_icon)
                            Image(
                                painter = checkMark,
                                contentDescription = "Correct",
                                modifier = Modifier.size(48.dp).padding(2.dp),
                                alpha = 0.95f
                            )
                        }
                    }
                } else if (isSelected && !isCorrect) {
                    this@Column.AnimatedVisibility(true) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            val tryAgainIcon = painterResource(id = R.drawable.try_again_icon)
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
            Text(
                text = if (showWordBelow) buildAnnotatedString {
                    word.forEachIndexed { i, c ->
                        if (i == differingIndex) {
                            withStyle(SpanStyle(color = if (isCorrect) Color(0xFF388E3C) else Color(0xFFD32F2F))) {
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
            is GameFeedback.Correct -> (feedbackState as GameFeedback.Correct).chosenWord
            is GameFeedback.Incorrect -> (feedbackState as GameFeedback.Incorrect).chosenWord
            else -> null
        }
        val disabledWords by viewModel.disabledWords.collectAsState()
        if (currentChallenge != null) {
            // 2x1 layout: first two centered across, third below
            if (isLandscape) {
        val score by viewModel?.score?.collectAsState() ?: remember { mutableStateOf(0) }
        val scoreHigh by viewModel?.scoreHigh?.collectAsState() ?: remember { mutableStateOf(0) }

                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    data class ImageChoiceData(val word: String, @DrawableRes val res: Int)
                    val all = listOf(
                        ImageChoiceData(currentChallenge.correctImageWord, currentChallenge.correctImageRes),
                        ImageChoiceData(currentChallenge.incorrectImageWord1, currentChallenge.incorrectImageRes1),
                        ImageChoiceData(currentChallenge.incorrectImageWord2, currentChallenge.incorrectImageRes2)
                    )
                    val items = currentChallenge.choiceOrder.map { all[it] }
                    for (item in items) {
                        Box(
                            modifier = Modifier.then(Modifier.weight(1f)).padding(8.dp)
                        ) {
                            ImageChoice(
                                word = item.word,
                                res = item.res,
                                enabled = true,
                                isCorrect = item.word == currentChallenge.correctImageWord,
                                isSelected = (chosenWord == item.word) || (disabledWords.contains(item.word) && item.word != currentChallenge.correctImageWord && feedbackState !is GameFeedback.None),
                                isDisabled = if (feedbackState is GameFeedback.Correct) false else disabledWords.contains(item.word) && chosenWord != currentChallenge.correctImageWord,
                                showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                    (item.word in disabledWords) ||
                                    (feedbackState is GameFeedback.Correct),
                                differingIndex = if (
                                    feedbackState is GameFeedback.Correct ||
                                    (feedbackState is GameFeedback.Incorrect && ((feedbackState as GameFeedback.Incorrect).chosenWord == item.word)) ||
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
            } else Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data class ImageChoiceData(
                    val word: String,
                    @DrawableRes val res: Int
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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 0..1) {
                        if (i < items.size) {
                            val item = items[i]
                            Box(
                                modifier = Modifier.then(Modifier.weight(1f)).padding(8.dp)
                            ) {
                                ImageChoice(
                                    word = item.word,
                                    res = item.res,
                                    enabled = true,
                                    isCorrect = item.word == currentChallenge.correctImageWord,
                                    isSelected = (chosenWord == item.word) || (disabledWords.contains(
                                        item.word
                                    ) && item.word != currentChallenge.correctImageWord && feedbackState !is GameFeedback.None),
                                    isDisabled = if (feedbackState is GameFeedback.Correct) false else disabledWords.contains(
                                        item.word
                                    ) && chosenWord != currentChallenge.correctImageWord,
                                    showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                            (item.word in disabledWords) ||
                                            (feedbackState is GameFeedback.Correct),
                                    differingIndex = if (
                                        feedbackState is GameFeedback.Correct ||
                                        (feedbackState is GameFeedback.Incorrect && ((feedbackState as GameFeedback.Incorrect).chosenWord == item.word)) ||
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.weight(0.5f))
                        Box(
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
                                ) && item.word != currentChallenge.correctImageWord && feedbackState !is GameFeedback.None),
                                isDisabled = if (feedbackState is GameFeedback.Correct) false else disabledWords.contains(
                                    item.word
                                ) && chosenWord != currentChallenge.correctImageWord,
                                showWordBelow = viewModel.gameSettings.collectAsState().value.alwaysShowWords ||
                                        (item.word in disabledWords) ||
                                        (feedbackState is GameFeedback.Correct),
                                differingIndex = if (
                                    feedbackState is GameFeedback.Correct ||
                                    (feedbackState is GameFeedback.Incorrect && ((feedbackState as GameFeedback.Incorrect).chosenWord == item.word)) ||
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
                        Spacer(modifier = Modifier.weight(0.5f))

                    }
                }
            }
        }
    } else {
        Text(text = "ImageChoicesArea", modifier = Modifier.padding(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialogContent(
    currentSettings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
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
    val autoAdvanceEnabled = currentSettings.autoAdvance
    val autoAdvanceInterval = currentSettings.autoAdvanceIntervalSeconds
    val alwaysShowWords = currentSettings.alwaysShowWords
    val pronounceTargetAtStart = currentSettings.pronounceTargetAtStart
    val ttsSpeed = currentSettings.ttsSpeed

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(0.dp)
            .fillMaxWidth()
            .heightIn(max = 520.dp) // prevents infinite height crash
    ) {
        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf("Game", "Sound", "About")
        val tabAccentBgColors = listOf(
            Color(0x22FF69B4), // Game - light pink
            Color(0x223D90FF), // Sound - light blue
            Color(0x22B566F8)  // About - light purple
        )
        Row(
            Modifier.fillMaxWidth().height(48.dp),
        ) {
            tabTitles.forEachIndexed { idx, title ->
                val selected = idx == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { selectedTab = idx }
                        .background(
                            if (selected) Color(0x22FF69B4) else Color.Transparent,
                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (selected) Color(0xFFFF69B4) else Color.Black,
                        fontWeight = if (selected) FontWeight.Bold else null
                    )
                    if (selected) {
                        Box(
                            Modifier.align(Alignment.BottomCenter).height(4.dp).fillMaxWidth()
                                .background(Color(0xFFFF69B4), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tabAccentBgColors[selectedTab])
        ) {
            when (selectedTab) {
                0 -> Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(28.dp)
                ) {
                    val timerOptions = listOf(0, 3, 5, 8, 15, 30)
                    val timerLabels = listOf("Off", "3", "5", "8", "15", "30")
                    val timerIndex =
                        timerOptions.indexOfFirst { it == (if (autoAdvanceEnabled) autoAdvanceInterval else 0) }
                            .coerceAtLeast(0)
                    Text(
                        "Auto-Advance Timer " + if (timerOptions[timerIndex] == 0) "(Off)" else "(${timerOptions[timerIndex]}s)",
                        modifier = Modifier.padding(
                            bottom = 2.dp,
                            top = 0.dp,
                            start = 0.dp,
                            end = 0.dp
                        )
                    )
                    Column(Modifier.padding(bottom = 2.dp, top = 0.dp)) {
                        Slider(
                            value = timerIndex.toFloat(),
                            onValueChange = {
                                val index = it.roundToInt()
                                val newVal = timerOptions[index]
                                val newSettings = currentSettings.copy(
                                    autoAdvance = newVal != 0,
                                    autoAdvanceIntervalSeconds = if (newVal == 0) 0 else newVal
                                )
                                onSettingsChange(newSettings)
                            },
                            steps = timerOptions.size - 2,
                            valueRange = 0f..(timerOptions.size - 1).toFloat(),
                            modifier = Modifier.fillMaxWidth().height(18.dp),
                            enabled = true,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFFFF69B4),
                                inactiveTrackColor = Color.LightGray,
                                thumbColor = Color(0xFFFF69B4),
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            ),
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState = sliderState,
                                    thumbTrackGapSize = 0.dp
                                )
                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(28.dp).padding(bottom = 2.dp)
                    ) {
                        Checkbox(
                            checked = alwaysShowWords,
                            onCheckedChange = {
                                onSettingsChange(
                                    currentSettings.copy(
                                        alwaysShowWords = it
                                    )
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF69B4), // pink
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(18.dp).padding(end = 8.dp)
                        )
                        Text("Always Show Words")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(28.dp).padding(bottom = 2.dp)
                    ) {
                        Checkbox(
                            checked = pronounceTargetAtStart,
                            onCheckedChange = {
                                onSettingsChange(
                                    currentSettings.copy(
                                        pronounceTargetAtStart = it
                                    )
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF69B4), // pink fallback
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(18.dp).padding(end = 8.dp)
                        )
                        Text("Spell Target Word")
                    }

                    var showResetConfirm by remember { mutableStateOf(false) }
                    Box(Modifier.fillMaxWidth()) {
                        val confirmPink = Color(0xFFFF69B4)
                        val rainbowBorder = listOf(
                            Color(0xFFFF1744),
                            Color(0xFFFFEA00),
                            Color(0xFF00E676),
                            Color(0xFF2979FF),
                            Color(0xFFD500F9)
                        )
                        if (!showResetConfirm) {
                            ConfirmButton(
                                fillColor = confirmPink,
                                borderColors = rainbowBorder,
                                modifier = Modifier.padding(top = 32.dp)
                                    .align(Alignment.Center),
                                onClick = { showResetConfirm = true },
                            ) {
                                Text(
                                    "Reset Scores",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column(
                                Modifier.padding(top = 32.dp).align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(id = R.string.dialog_restart_message),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                ) {
                                    ConfirmButton(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                            .height(46.dp),
                                        fillColor = confirmPink,
                                        borderColors = rainbowBorder,
                                        onClick = {
                                            showResetConfirm = false
                                            onResetGame()
                                        }
                                    ) {
                                        Text(
                                            stringResource(id = R.string.button_yes),
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    ConfirmButton(
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        fillColor = Color(0xFFF3F3F3),
                                        borderColors = rainbowBorder,
                                        onClick = { showResetConfirm = false }
                                    ) {
                                        Text(
                                            "No",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(28.dp)
                ) {
                    Text("Sound settings...", Modifier.padding(8.dp))
                }

                2 -> Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(28.dp)
                ) {
                    Text("About content...", Modifier.padding(8.dp))
                }
            }
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
    Image(
        painter = painterResource(id = R.drawable.btn_next),
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
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (!isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {
                // +N popup above the score
                AnimatedVisibility(
                    visible = (scoreDelta ?: 0) > 0,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .padding(start = 56.dp)
                ) {
                    Text(
                        text = "+${scoreDelta ?: 0}",
                        fontSize = 22.sp,
                        color = Color(0xFF388E3C),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                    )
                }


                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_hint),
                        contentDescription = "Help",
                        modifier = Modifier
                            .size(60.dp)
                            .clickable(enabled = isHintEnabled) { viewModel.requestHint() }
                            .alpha(if (isHintEnabled) 1f else 0.4f)
                    )
                    Spacer(modifier = Modifier.then(Modifier.weight(1f)))
                    if (feedbackState is GameFeedback.Correct) {
                        NextButton(modifier = Modifier.align(Alignment.CenterVertically)) { viewModel.requestNextWordManually() }
                    } else {
                        Spacer(modifier = Modifier.size(80.dp))
                    }
                    Spacer(modifier = Modifier.then(Modifier.weight(1f)))
                    Image(
                        painter = painterResource(id = R.drawable.btn_options),
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
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
            ) {
                // +N popup above the score
                AnimatedVisibility(
                    visible = (scoreDelta ?: 0) > 0,
                    modifier = Modifier.align(Alignment.TopCenter).padding(start = 56.dp)
                ) {
                    Text(
                        text = "+${scoreDelta ?: 0}",
                        fontSize = 22.sp,
                        color = Color(0xFF388E3C),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                    )
                }

                // Vertical options/buttons only
                if (viewModel != null) {
                    val feedbackState by viewModel.feedbackState.collectAsState()
                    val isHintEnabled by viewModel.isHintButtonEnabled.collectAsState()
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Options Button top
                        Image(
                            painter = painterResource(id = R.drawable.btn_options),
                            contentDescription = stringResource(id = R.string.button_options),
                            modifier = Modifier
                                .size(60.dp)
                                .clickable {
                                    viewModel.cancelAutoAdvanceTimer()
                                    onDialogOpenChange(true)
                                }
                        )
                        // Next Button, only if correct
                        if (feedbackState is GameFeedback.Correct) {
                            NextButton(modifier = Modifier.align(Alignment.CenterHorizontally)) { viewModel.requestNextWordManually() }
                        } else {
                            Spacer(modifier = Modifier.size(80.dp))
                        }
                        Image(
                            painter = painterResource(id = R.drawable.btn_hint),
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
        Text(text = "GameBorder", modifier = Modifier.padding(16.dp))
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
            val sweep = Brush.sweepGradient(rainbow)
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
        enter = fadeIn(tween(210)) +
                slideInVertically(tween(350), initialOffsetY = { it / 4 }),
        exit = fadeOut(tween(210)) +
                slideOutVertically(tween(210), targetOffsetY = { it / 4 })
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xAA000000))
                .clickable(onClick = onDismiss, indication = null, interactionSource = remember { MutableInteractionSource() })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.8f)
                    .align(Alignment.Center)
                    .rainbowDashedBorder()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clickable(enabled = false, indication = null, interactionSource = remember { MutableInteractionSource() }) {}
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