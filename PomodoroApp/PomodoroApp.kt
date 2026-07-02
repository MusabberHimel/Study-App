package com.musabber.pomofocus

import android.app.Application
import com.musabber.pomofocus.data.TimerManager
import com.musabber.pomofocus.data.local.AppDatabase
import com.musabber.pomofocus.data.local.SettingsDataStore

class PomodoroApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        settingsDataStore = SettingsDataStore(this)

        TimerManager.initialize(this, database, settingsDataStore)
    }
}
