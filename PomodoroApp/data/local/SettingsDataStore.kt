package com.musabber.pomofocus.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pomofocus_preferences")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val STUDY_DURATION = intPreferencesKey("study_duration")
        val SHORT_BREAK_DURATION = intPreferencesKey("short_break_duration")
        val LONG_BREAK_DURATION = intPreferencesKey("long_break_duration")
        val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        val ALARM_SOUND_URI = stringPreferencesKey("alarm_sound_uri")
        val ALARM_SOUND_NAME = stringPreferencesKey("alarm_sound_name")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val VIBRATION_PATTERN = stringPreferencesKey("vibration_pattern")
        val VIBRATION_CUSTOM_PATTERN = stringPreferencesKey("vibration_custom_pattern")
        val VIBRATION_DURATION_MS = intPreferencesKey("vibration_duration_ms")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val SELECTED_ICON = stringPreferencesKey("selected_icon")
    }

    val studyDuration: Flow<Int> = dataStore.data.map { it[STUDY_DURATION] ?: 25 }
    val shortBreakDuration: Flow<Int> = dataStore.data.map { it[SHORT_BREAK_DURATION] ?: 5 }
    val longBreakDuration: Flow<Int> = dataStore.data.map { it[LONG_BREAK_DURATION] ?: 15 }
    val dndEnabled: Flow<Boolean> = dataStore.data.map { it[DND_ENABLED] ?: false }
    val alarmSoundUri: Flow<String> = dataStore.data.map { it[ALARM_SOUND_URI] ?: "" }
    val alarmSoundName: Flow<String> = dataStore.data.map { it[ALARM_SOUND_NAME] ?: "Default" }
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { it[VIBRATION_ENABLED] ?: true }
    val vibrationPattern: Flow<String> = dataStore.data.map { it[VIBRATION_PATTERN] ?: "Single pulse" }
    val vibrationCustomPattern: Flow<String> = dataStore.data.map { it[VIBRATION_CUSTOM_PATTERN] ?: "0,500,200,500" }
    val vibrationDurationMs: Flow<Int> = dataStore.data.map { it[VIBRATION_DURATION_MS] ?: 500 }
    val selectedTheme: Flow<String> = dataStore.data.map { it[SELECTED_THEME] ?: "Dark" }
    val selectedIcon: Flow<String> = dataStore.data.map { it[SELECTED_ICON] ?: "Book" }

    suspend fun saveStudyDuration(minutes: Int) {
        dataStore.edit { it[STUDY_DURATION] = minutes.coerceIn(5, 90) }
    }

    suspend fun saveShortBreakDuration(minutes: Int) {
        dataStore.edit { it[SHORT_BREAK_DURATION] = minutes.coerceIn(1, 30) }
    }

    suspend fun saveLongBreakDuration(minutes: Int) {
        dataStore.edit { it[LONG_BREAK_DURATION] = minutes.coerceIn(5, 60) }
    }

    suspend fun saveDndEnabled(enabled: Boolean) {
        dataStore.edit { it[DND_ENABLED] = enabled }
    }

    suspend fun saveAlarmSound(uri: String, name: String) {
        dataStore.edit {
            it[ALARM_SOUND_URI] = uri
            it[ALARM_SOUND_NAME] = name
        }
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun saveVibrationPattern(pattern: String) {
        dataStore.edit { it[VIBRATION_PATTERN] = pattern }
    }

    suspend fun saveVibrationCustomPattern(pattern: String) {
        dataStore.edit { it[VIBRATION_CUSTOM_PATTERN] = pattern }
    }

    suspend fun saveVibrationDuration(durationMs: Int) {
        dataStore.edit { it[VIBRATION_DURATION_MS] = durationMs }
    }

    suspend fun saveTheme(theme: String) {
        dataStore.edit { it[SELECTED_THEME] = theme }
    }

    suspend fun saveIcon(icon: String) {
        dataStore.edit { it[SELECTED_ICON] = icon }
    }
}