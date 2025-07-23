package com.edanstarfire.tinywords

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ScoreStreakPrefsKeys {
    val SCORE = intPreferencesKey("score")
    val STREAK = intPreferencesKey("streak")
    val SCORE_HIGH = intPreferencesKey("score_high")
    val STREAK_HIGH = intPreferencesKey("streak_high")
}

val Context.scoreStreakDataStore by preferencesDataStore(name = "score_streak_prefs")

class ScoreStreakRepository(private val context: Context) {
    val score: Flow<Int> = context.scoreStreakDataStore.data.map { it[ScoreStreakPrefsKeys.SCORE] ?: 0 }
    val streak: Flow<Int> = context.scoreStreakDataStore.data.map { it[ScoreStreakPrefsKeys.STREAK] ?: 0 }
    val scoreHigh: Flow<Int> = context.scoreStreakDataStore.data.map { it[ScoreStreakPrefsKeys.SCORE_HIGH] ?: 0 }
    val streakHigh: Flow<Int> = context.scoreStreakDataStore.data.map { it[ScoreStreakPrefsKeys.STREAK_HIGH] ?: 0 }

    suspend fun setScore(score: Int) {
        context.scoreStreakDataStore.edit { it[ScoreStreakPrefsKeys.SCORE] = score }
    }

    suspend fun setStreak(streak: Int) {
        context.scoreStreakDataStore.edit { it[ScoreStreakPrefsKeys.STREAK] = streak }
    }

    suspend fun setScoreHigh(scoreHigh: Int) {
        context.scoreStreakDataStore.edit { it[ScoreStreakPrefsKeys.SCORE_HIGH] = scoreHigh }
    }

    suspend fun setStreakHigh(streakHigh: Int) {
        context.scoreStreakDataStore.edit { it[ScoreStreakPrefsKeys.STREAK_HIGH] = streakHigh }
    }

    suspend fun reset() {
        context.scoreStreakDataStore.edit {
            it[ScoreStreakPrefsKeys.SCORE] = 0
            it[ScoreStreakPrefsKeys.STREAK] = 0
            it[ScoreStreakPrefsKeys.SCORE_HIGH] = 0
            it[ScoreStreakPrefsKeys.STREAK_HIGH] = 0
        }
    }
}
