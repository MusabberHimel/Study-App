package com.musabber.pomofocus.util

import android.app.NotificationManager
import android.content.Context

object DndHelper {
    private var originalFilter: Int? = null

    fun enableDnd(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            try {
                if (originalFilter == null) {
                    originalFilter = notificationManager.currentInterruptionFilter
                }
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreDnd(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            try {
                originalFilter?.let {
                    notificationManager.setInterruptionFilter(it)
                    originalFilter = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}