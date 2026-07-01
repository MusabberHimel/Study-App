package com.musabber.pomofocus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musabber.pomofocus.PomodoroApp
import com.musabber.pomofocus.data.TimerManager
import com.musabber.pomofocus.data.local.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PomodoroApp
    val settingsDataStore: SettingsDataStore = app.settingsDataStore

    val studyDuration: StateFlow<Int> = settingsDataStore.studyDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val shortBreakDuration: StateFlow<Int> = settingsDataStore.shortBreakDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val longBreakDuration: StateFlow<Int> = settingsDataStore.longBreakDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)

    val dndEnabled: StateFlow<Boolean> = settingsDataStore.dndEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val alarmSoundUri: StateFlow<String> = settingsDataStore.alarmSoundUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val alarmSoundName: StateFlow<String> = settingsDataStore.alarmSoundName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Default")

    val vibrationEnabled: StateFlow<Boolean> = settingsDataStore.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationPattern: StateFlow<String> = settingsDataStore.vibrationPattern
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Single pulse")

    val vibrationCustomPattern: StateFlow<String> = settingsDataStore.vibrationCustomPattern
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0,500,200,500")

    val vibrationDurationMs: StateFlow<Int> = settingsDataStore.vibrationDurationMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 500)

    val selectedTheme: StateFlow<String> = settingsDataStore.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dark")

    val selectedIcon: StateFlow<String> = settingsDataStore.selectedIcon
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Book")

    fun setStudyDuration(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.saveStudyDuration(minutes)
            TimerManager.reset()
        }
    }

    fun setShortBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.saveShortBreakDuration(minutes)
            TimerManager.reset()
        }
    }

    fun setLongBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.saveLongBreakDuration(minutes)
            TimerManager.reset()
        }
    }

    fun setDndEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveDndEnabled(enabled)
            TimerManager.updateDndState()
        }
    }

    fun setAlarmSound(uri: String, name: String) {
        viewModelScope.launch {
            settingsDataStore.saveAlarmSound(uri, name)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveVibrationEnabled(enabled)
        }
    }

    fun setVibrationPattern(pattern: String) {
        viewModelScope.launch {
            settingsDataStore.saveVibrationPattern(pattern)
        }
    }

    fun setVibrationCustomPattern(pattern: String) {
        viewModelScope.launch {
            settingsDataStore.saveVibrationCustomPattern(pattern)
        }
    }

    fun setVibrationDuration(durationMs: Int) {
        viewModelScope.launch {
            settingsDataStore.saveVibrationDuration(durationMs)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsDataStore.saveTheme(theme)
        }
    }

    fun setIcon(icon: String) {
        viewModelScope.launch {
            settingsDataStore.saveIcon(icon)
        }
    }
}