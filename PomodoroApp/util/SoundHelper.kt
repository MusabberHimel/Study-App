package com.musabber.pomofocus.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.musabber.pomofocus.PomodoroApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playAlarm(context: Context) {
        val dataStore = (context.applicationContext as PomodoroApp).settingsDataStore
        CoroutineScope(Dispatchers.IO).launch {
            val uriStr = dataStore.alarmSoundUri.first()
            withContext(Dispatchers.Main) {
                try {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null

                    val uri = if (uriStr.isNotEmpty()) {
                        Uri.parse(uriStr)
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, uri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        isLooping = false
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(context, fallbackUri)
                            prepare()
                            start()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}