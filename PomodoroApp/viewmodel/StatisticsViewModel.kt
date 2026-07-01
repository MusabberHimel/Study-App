package com.musabber.pomofocus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musabber.pomofocus.PomodoroApp
import com.musabber.pomofocus.data.local.SessionLog
import kotlinx.coroutines.flow.*
import java.util.Calendar

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PomodoroApp
    private val sessionDao = app.database.sessionDao()

    data class StatisticsState(
        val studyTodayStr: String = "0h 0m",
        val breakTodayStr: String = "0h 0m",
        val studyMonthStr: String = "0h 0m",
        val studyYearStr: String = "0h 0m"
    )

    private val _statsState = MutableStateFlow(StatisticsState())
    val statsState: StateFlow<StatisticsState> = _statsState

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfYear = calendar.timeInMillis

        sessionDao.getSessionsSince(startOfYear)
            .map { logs ->
                calculateStats(logs)
            }
            .onEach { state ->
                _statsState.value = state
            }
            .launchIn(viewModelScope)
    }

    private fun calculateStats(logs: List<SessionLog>): StatisticsState {
        val nowCal = Calendar.getInstance()
        val todayYear = nowCal.get(Calendar.YEAR)
        val todayMonth = nowCal.get(Calendar.MONTH)
        val todayDay = nowCal.get(Calendar.DAY_OF_YEAR)

        var studyTodayMillis = 0L
        var breakTodayMillis = 0L
        var studyMonthMillis = 0L
        var studyYearMillis = 0L

        val logCal = Calendar.getInstance()

        for (log in logs) {
            logCal.timeInMillis = log.startTimestamp
            val logYear = logCal.get(Calendar.YEAR)
            val logMonth = logCal.get(Calendar.MONTH)
            val logDay = logCal.get(Calendar.DAY_OF_YEAR)

            val isStudy = log.sessionType == "STUDY"
            val isBreak = log.sessionType == "SHORT_BREAK" || log.sessionType == "LONG_BREAK"

            if (logYear == todayYear && logDay == todayDay) {
                if (isStudy) {
                    studyTodayMillis += log.durationMillis
                } else if (isBreak) {
                    breakTodayMillis += log.durationMillis
                }
            }

            if (logYear == todayYear && logMonth == todayMonth) {
                if (isStudy) {
                    studyMonthMillis += log.durationMillis
                }
            }

            if (logYear == todayYear) {
                if (isStudy) {
                    studyYearMillis += log.durationMillis
                }
            }
        }

        return StatisticsState(
            studyTodayStr = formatMillisToHoursAndMinutes(studyTodayMillis),
            breakTodayStr = formatMillisToHoursAndMinutes(breakTodayMillis),
            studyMonthStr = formatMillisToHoursAndMinutes(studyMonthMillis),
            studyYearStr = formatMillisToHoursAndMinutes(studyYearMillis)
        )
    }

    private fun formatMillisToHoursAndMinutes(millis: Long): String {
        val totalMinutes = millis / 1000 / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }
}