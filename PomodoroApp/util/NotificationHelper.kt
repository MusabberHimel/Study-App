package com.musabber.pomofocus.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.musabber.pomofocus.MainActivity
import com.musabber.pomofocus.data.SessionType
import com.musabber.pomofocus.service.TimerService

object NotificationHelper {
    const val CHANNEL_TIMER_ID = "timer_service_channel"
    const val CHANNEL_ALERTS_ID = "timer_alerts_channel"
    const val TIMER_NOTIFICATION_ID = 1
    const val ALERTS_NOTIFICATION_ID = 2

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val timerChannel = NotificationChannel(
                CHANNEL_TIMER_ID,
                "Timer Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active countdown timer status."
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggers high-priority notifications when sessions end."
                enableVibration(true)
                setBypassDnd(true)
            }

            manager.createNotificationChannel(timerChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    fun buildTimerNotification(
        context: Context,
        sessionType: SessionType,
        remainingMillis: Long,
        isRunning: Boolean
    ): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(context, TimerService::class.java).apply {
            action = if (isRunning) TimerService.ACTION_PAUSE else TimerService.ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            context,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseActionText = if (isRunning) "Pause" else "Play"

        val skipIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_SKIP
        }
        val skipPendingIntent = PendingIntent.getService(
            context,
            2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val seconds = (remainingMillis / 1000) % 60
        val minutes = (remainingMillis / 1000) / 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        val title = when (sessionType) {
            SessionType.STUDY -> "Study Session"
            SessionType.SHORT_BREAK -> "Short Break"
            SessionType.LONG_BREAK -> "Long Break"
        }

        return NotificationCompat.Builder(context, CHANNEL_TIMER_ID)
            .setContentTitle(title)
            .setContentText("Time remaining: $timeStr")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_play, playPauseActionText, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPendingIntent)
            .build()
    }

    fun showTimesUpNotification(context: Context, sessionType: SessionType) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sessionName = when (sessionType) {
            SessionType.STUDY -> "Study session"
            SessionType.SHORT_BREAK -> "Short break"
            SessionType.LONG_BREAK -> "Long break"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setContentTitle("Time's Up!")
            .setContentText("Your $sessionName has finished.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        manager.notify(ALERTS_NOTIFICATION_ID, notification)
    }
}