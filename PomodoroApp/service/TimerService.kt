package com.musabber.pomofocus.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.musabber.pomofocus.data.TimerManager
import com.musabber.pomofocus.util.NotificationHelper
import kotlinx.coroutines.*

class TimerService : Service() {
    private val serviceJob = SupervisorJob()

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_SKIP = "ACTION_SKIP"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = NotificationHelper.buildTimerNotification(
                    this,
                    TimerManager.sessionType.value,
                    TimerManager.remainingMillis.value,
                    TimerManager.isRunning.value
                )
                startForeground(NotificationHelper.TIMER_NOTIFICATION_ID, notification)
            }
            ACTION_PLAY -> {
                TimerManager.start()
            }
            ACTION_PAUSE -> {
                TimerManager.pause()
            }
            ACTION_SKIP -> {
                TimerManager.skip()
            }
            ACTION_UPDATE -> {
                val notification = NotificationHelper.buildTimerNotification(
                    this,
                    TimerManager.sessionType.value,
                    TimerManager.remainingMillis.value,
                    TimerManager.isRunning.value
                )
                startForeground(NotificationHelper.TIMER_NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}