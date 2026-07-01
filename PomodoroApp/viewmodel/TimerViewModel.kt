package com.musabber.pomofocus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musabber.pomofocus.PomodoroApp
import com.musabber.pomofocus.data.SessionType
import com.musabber.pomofocus.data.TimerManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PomodoroApp

    val remainingMillis: StateFlow<Long> = TimerManager.remainingMillis
    val totalDurationMillis: StateFlow<Long> = TimerManager.totalDurationMillis
    val isRunning: StateFlow<Boolean> = TimerManager.isRunning
    val sessionType: StateFlow<SessionType> = TimerManager.sessionType
    val studySessionCount: StateFlow<Int> = TimerManager.studySessionCount

    val selectedIcon: StateFlow<String> = app.settingsDataStore.selectedIcon
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Book")

    fun toggleTimer() {
        if (isRunning.value) {
            TimerManager.pause()
        } else {
            TimerManager.start()
        }
    }

    fun resetTimer() {
        TimerManager.reset()
    }

    fun skipTimer() {
        TimerManager.skip()
    }
}