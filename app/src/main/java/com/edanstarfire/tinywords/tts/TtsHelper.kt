package com.edanstarfire.tinywords.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Optional: @Singleton if you want only one TtsHelper instance for the whole app
@Singleton
class TtsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    // We'll change how onInitialized is handled with Hilt or make it internal
    // For now, let's simplify and assume for this step the caller doesn't need immediate feedback
    // Or we manage TTS readiness internally with a Flow.
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isInitialized =
        MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        try {
            // Use applicationContext to avoid memory leaks if TtsHelper is a Singleton
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TtsHelper", "Failed to initialize TextToSpeech", e)
            _isInitialized.value = false // Explicitly set to false on creation error
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsHelper", "Language (US English) not supported or missing data.")
                _isInitialized.value = false
            } else {
                Log.i("TtsHelper", "TextToSpeech initialized successfully.")
                _isInitialized.value = true
            }
        } else {
            Log.e("TtsHelper", "TextToSpeech initialization failed with status: $status")
            _isInitialized.value = false
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (_isInitialized.value && tts != null) {
            tts?.speak(text, queueMode, null, null)
            Log.i("TtsHelper", "Speaking: '$text'")
        } else {
            Log.w("TtsHelper", "TTS not initialized or null, cannot speak: '$text'. Current init state: ${_isInitialized.value}")
        }
    }

    fun shutdown() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
            tts = null
            Log.i("TtsHelper", "TextToSpeech shutdown.")
        }
        _isInitialized.value = false // Reflect that it's no longer initialized
    }
}
