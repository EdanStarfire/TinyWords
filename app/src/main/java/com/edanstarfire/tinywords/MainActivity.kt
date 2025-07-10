package com.edanstarfire.tinywords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TinyWordsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        viewModel = gameViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, viewModel: GameViewModel?) {
    Text(
        text = "Hello $name! I'm TinyWords.",
        modifier = modifier
    )
    if (viewModel != null) {
        // 1. Collect the TTS readiness state correctly
        val ttsReady by viewModel.isTtsReady.collectAsState() // This subscribes to changes

        // 2. Use LaunchedEffect to act when ttsReady becomes true
        LaunchedEffect(ttsReady, name) { // Keys: recomposes/re-launches if ttsReady or name change
            if (ttsReady) {
                viewModel.pronounceWord("Hello $name!")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TinyWordsTheme {
        Greeting("Android", viewModel = null)
    }
}