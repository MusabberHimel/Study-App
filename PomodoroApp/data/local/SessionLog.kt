package com.musabber.pomofocus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionType: String,
    val startTimestamp: Long,
    val durationMillis: Long
)