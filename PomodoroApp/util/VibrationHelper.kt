package com.musabber.pomofocus.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.musabber.pomofocus.PomodoroApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object VibrationHelper {
    fun vibrate(context: Context) {
        val dataStore = (context.applicationContext as PomodoroApp).settingsDataStore
        
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = dataStore.vibrationEnabled.first()
            if (!enabled) return@launch

            val patternType = dataStore.vibrationPattern.first()
            val customPatternStr = dataStore.vibrationCustomPattern.first()
            val duration = dataStore.vibrationDurationMs.first().toLong()

            val pattern = when (patternType) {
                "Single pulse" -> longArrayOf(0, duration)
                "Double pulse" -> longArrayOf(0, duration, 200, duration)
                "Triple pulse" -> longArrayOf(0, duration, 200, duration, 200, duration)
                "Long pulse" -> longArrayOf(0, duration * 2)
                "Custom" -> {
                    try {
                        customPatternStr.split(",")
                            .map { it.trim().toLongOrNull() ?: 0L }
                            .toLongArray()
                    } catch (e: Exception) {
                        longArrayOf(0, duration)
                    }
                }
                else -> longArrayOf(0, duration)
            }

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}