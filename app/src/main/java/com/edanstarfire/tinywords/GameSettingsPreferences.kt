package com.edanstarfire.tinywords

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.edanstarfire.tinywords.ui.game.GameSettings

object GameSettingsPrefsKeys {
    val TTS_SPEED = floatPreferencesKey("tts_speed")
    val AUTO_ADVANCE = booleanPreferencesKey("auto_advance")
    val AUTO_ADVANCE_INTERVAL = intPreferencesKey("auto_advance_interval_seconds")
    val HINT_LEVEL = intPreferencesKey("hint_level_allowed")
    val ALWAYS_SHOW_WORDS = booleanPreferencesKey("always_show_words")
    val PRONOUNCE_TARGET_AT_START = booleanPreferencesKey("pronounce_target_at_start")
    val MUSIC_VOLUME = intPreferencesKey("music_volume")
    val TTS_VOLUME = intPreferencesKey("tts_volume")
    val BG_MUSIC_TRACK = androidx.datastore.preferences.core.stringPreferencesKey("bg_music_track")
    val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
}

val Context.gameSettingsDataStore by preferencesDataStore(name = "game_settings_prefs")

class GameSettingsRepository(private val context: Context) {
    val settings: Flow<GameSettings> = context.gameSettingsDataStore.data.map { prefs ->
        GameSettings(
            ttsSpeed = prefs[GameSettingsPrefsKeys.TTS_SPEED] ?: 1.0f,
            autoAdvance = prefs[GameSettingsPrefsKeys.AUTO_ADVANCE] ?: true,
            autoAdvanceIntervalSeconds = prefs[GameSettingsPrefsKeys.AUTO_ADVANCE_INTERVAL] ?: 30,
            hintLevelAllowed = prefs[GameSettingsPrefsKeys.HINT_LEVEL] ?: 2,
            alwaysShowWords = prefs[GameSettingsPrefsKeys.ALWAYS_SHOW_WORDS] ?: false,
            pronounceTargetAtStart = prefs[GameSettingsPrefsKeys.PRONOUNCE_TARGET_AT_START] ?: false,
            musicVolume = prefs[GameSettingsPrefsKeys.MUSIC_VOLUME] ?: 100,
            ttsVolume = prefs[GameSettingsPrefsKeys.TTS_VOLUME] ?: 100,
            bgMusicTrack = prefs[GameSettingsPrefsKeys.BG_MUSIC_TRACK] ?: "default_bg_music.mp3",
            ttsEnabled = prefs[GameSettingsPrefsKeys.TTS_ENABLED] ?: true
        )
    }

    suspend fun setSettings(settings: GameSettings) {
        context.gameSettingsDataStore.edit {
            it[GameSettingsPrefsKeys.TTS_SPEED] = settings.ttsSpeed
            it[GameSettingsPrefsKeys.AUTO_ADVANCE] = settings.autoAdvance
            it[GameSettingsPrefsKeys.AUTO_ADVANCE_INTERVAL] = settings.autoAdvanceIntervalSeconds
            it[GameSettingsPrefsKeys.HINT_LEVEL] = settings.hintLevelAllowed
            it[GameSettingsPrefsKeys.ALWAYS_SHOW_WORDS] = settings.alwaysShowWords
            it[GameSettingsPrefsKeys.PRONOUNCE_TARGET_AT_START] = settings.pronounceTargetAtStart
            it[GameSettingsPrefsKeys.MUSIC_VOLUME] = settings.musicVolume
            it[GameSettingsPrefsKeys.TTS_VOLUME] = settings.ttsVolume
            it[GameSettingsPrefsKeys.BG_MUSIC_TRACK] = settings.bgMusicTrack
            it[GameSettingsPrefsKeys.TTS_ENABLED] = settings.ttsEnabled
        }
    }
}
