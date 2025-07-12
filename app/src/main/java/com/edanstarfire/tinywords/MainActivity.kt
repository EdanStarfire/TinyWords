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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
}

@Composable
fun GameScreen(modifier: Modifier = Modifier, viewModel: GameViewModel?) {
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
        val target = currentChallenge?.targetWord
        val incorrect1 = currentChallenge?.incorrectImageWord1
        val incorrect2 = currentChallenge?.incorrectImageWord2
        val differingIndex = if (hintLevel >= 1 && target != null && incorrect1 != null && incorrect2 != null) {
            target.indices.firstOrNull { idx ->
                (idx < incorrect1.length && target[idx] != incorrect1[idx]) ||
                (idx < incorrect2.length && target[idx] != incorrect2[idx])
            }
        } else null

        androidx.compose.runtime.LaunchedEffect(target) {
            if (target != null) viewModel.pronounceWord(target)
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
                    modifier = Modifier.padding(16.dp)
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
fun ImageChoicesArea(viewModel: GameViewModel?) {
    if (viewModel != null) {
        val currentChallengeState by viewModel.currentChallenge.collectAsState()
        val currentChallenge = currentChallengeState
        if (currentChallenge != null) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                val items = listOf(
                    Triple(currentChallenge.correctImageWord, currentChallenge.correctImageRes, true),
                    Triple(currentChallenge.incorrectImageWord1, currentChallenge.incorrectImageRes1, false),
                    Triple(currentChallenge.incorrectImageWord2, currentChallenge.incorrectImageRes2, false)
                )
                for ((word, res, isCorrect) in items) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        tonalElevation = 2.dp,
        onClick = {}
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(0.9f)
                .aspectRatio(1f),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (res != 0) {
                val painter = androidx.compose.ui.res.painterResource(id = res)
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = word,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = word, fontSize = 25.sp)
            }
        }
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
fun GameBorder(viewModel: GameViewModel?) {
    if (viewModel != null) {
        val streak by viewModel.streak.collectAsState()
        val isHintEnabled by viewModel.isHintButtonEnabled.collectAsState()
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
        ) {
            // Streak counter (top-left)
            androidx.compose.material3.Text(
                text = "Streak: $streak",
                fontSize = 20.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart)
            )

            // Restart button (top-right)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder_1),
                contentDescription = "Restart",
                modifier = Modifier
                    .size(60.dp)
                    .align(androidx.compose.ui.Alignment.TopEnd)
                    .clickable { viewModel.resetGame() }
            )

            // Help button (bottom-center)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder_1),
                contentDescription = "Help",
                modifier = Modifier
                    .size(60.dp)
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .clickable(enabled = isHintEnabled) { viewModel.requestHint() }
                    .alpha(if (isHintEnabled) 1f else 0.4f)
            )

            // Options/settings button (bottom-end)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.placeholder_1),
                contentDescription = "Options",
                modifier = Modifier
                    .size(60.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .clickable { /* open settings */ }
            )
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