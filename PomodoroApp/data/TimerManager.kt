package com.musabber.pomofocus.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.musabber.pomofocus.data.local.AppDatabase
import com.musabber.pomofocus.data.local.SessionLog
import com.musabber.pomofocus.data.local.SettingsDataStore
import com.musabber.pomofocus.service.TimerService
import com.musabber.pomofocus.util.DndHelper
import com.musabber.pomofocus.util.NotificationHelper
import com.musabber.pomofocus.util.SoundHelper
import com.musabber.pomofocus.util.VibrationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

enum class SessionType {
    STUDY, SHORT_BREAK, LONG_BREAK
}

object TimerManager {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var dataStore: SettingsDataStore

    private val _remainingMillis = MutableStateFlow(25 * 60 * 1000L)
    val remainingMillis: StateFlow<Long> = _remainingMillis

    private val _totalDurationMillis = MutableStateFlow(25 * 60 * 1000L)
    val totalDurationMillis: StateFlow<Long> = _totalDurationMillis

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _sessionType = MutableStateFlow(SessionType.STUDY)
    val sessionType: StateFlow<SessionType> = _sessionType

    private val _studySessionCount = MutableStateFlow(0)
    val studySessionCount: StateFlow<Int> = _studySessionCount

    private var timerJob: Job? = null
    private var lastTickTime = 0L

    fun initialize(context: Context, database: AppDatabase, dataStore: SettingsDataStore) {
        this.context = context.applicationContext
        this.database = database
        this.dataStore = dataStore

        CoroutineScope(Dispatchers.IO).launch {
            val studyMin = dataStore.studyDuration.first()
            _totalDurationMillis.value = studyMin * 60 * 1000L
            _remainingMillis.value = _totalDurationMillis.value
        }
    }

    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        lastTickTime = System.currentTimeMillis()

        updateDndState()

        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (_isRunning.value) {
                delay(250)
                val now = System.currentTimeMillis()
                val elapsed = now - lastTickTime
                if (elapsed >= 1000) {
                    val secondsElapsed = elapsed / 1000
                    lastTickTime += secondsElapsed * 1000
                    val currentRemaining = _remainingMillis.value
                    val nextRemaining = (currentRemaining - secondsElapsed * 1000).coerceAtLeast(0)
                    _remainingMillis.value = nextRemaining

                    updateServiceNotification()

                    if (nextRemaining == 0L) {
                        onTimerFinished()
                        break
                    }
                }
            }
        }
    }

    fun pause() {
        if (!_isRunning.value) return
        _isRunning.value = false
        timerJob?.cancel()

        DndHelper.restoreDnd(context)
        updateServiceNotification()
    }

    fun reset() {
        pause()
        CoroutineScope(Dispatchers.IO).launch {
            loadDurationAndReset(_sessionType.value)
        }
    }

    fun skip() {
        val elapsed = _totalDurationMillis.value - _remainingMillis.value
        val startTime = System.currentTimeMillis() - elapsed
        val currentType = _sessionType.value

        pause()

        if (elapsed > 0) {
            logSession(currentType, startTime, elapsed)
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (currentType == SessionType.STUDY) {
                val nextCount = _studySessionCount.value + 1
                if (nextCount >= 4) {
                    _studySessionCount.value = 0
                    startSession(SessionType.LONG_BREAK)
                } else {
                    _studySessionCount.value = nextCount
                    startSession(SessionType.SHORT_BREAK)
                }
            } else {
                startSession(SessionType.STUDY)
            }
        }
    }

    private suspend fun startSession(type: SessionType) {
        _sessionType.value = type
        loadDurationAndReset(type)
        withContext(Dispatchers.Main) {
            start()
        }
    }

    private suspend fun loadDurationAndReset(type: SessionType) {
        val durationMin = when (type) {
            SessionType.STUDY -> dataStore.studyDuration.first()
            SessionType.SHORT_BREAK -> dataStore.shortBreakDuration.first()
            SessionType.LONG_BREAK -> dataStore.longBreakDuration.first()
        }
        _totalDurationMillis.value = durationMin * 60 * 1000L
        _remainingMillis.value = _totalDurationMillis.value
        updateServiceNotification()
    }

    private fun onTimerFinished() {
        _isRunning.value = false
        timerJob?.cancel()

        val completedType = _sessionType.value
        val duration = _totalDurationMillis.value
        val startTime = System.currentTimeMillis() - duration

        logSession(completedType, startTime, duration)

        VibrationHelper.vibrate(context)
        SoundHelper.playAlarm(context)

        NotificationHelper.showTimesUpNotification(context, completedType)

        CoroutineScope(Dispatchers.IO).launch {
            if (completedType == SessionType.STUDY) {
                val nextCount = _studySessionCount.value + 1
                if (nextCount >= 4) {
                    _studySessionCount.value = 0
                    startSession(SessionType.LONG_BREAK)
                } else {
                    _studySessionCount.value = nextCount
                    startSession(SessionType.SHORT_BREAK)
                }
            } else {
                startSession(SessionType.STUDY)
            }
        }
    }

    private fun logSession(type: SessionType, startTimestamp: Long, durationMillis: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val sessionLog = SessionLog(
                sessionType = type.name,
                startTimestamp = startTimestamp,
                durationMillis = durationMillis
            )
            database.sessionDao().insert(sessionLog)
        }
    }

    fun updateDndState() {
        CoroutineScope(Dispatchers.IO).launch {
            val dndEnabled = dataStore.dndEnabled.first()
            if (dndEnabled && _sessionType.value == SessionType.STUDY && _isRunning.value) {
                withContext(Dispatchers.Main) {
                    DndHelper.enableDnd(context)
                }
            } else {
                withContext(Dispatchers.Main) {
                    DndHelper.restoreDnd(context)
                }
            }
        }
    }

    private fun updateServiceNotification() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_UPDATE
        }
        context.startService(intent)
    }
}