package com.musabber.pomofocus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(log: SessionLog)

    @Query("SELECT * FROM session_logs WHERE startTimestamp >= :timestamp")
    fun getSessionsSince(timestamp: Long): Flow<List<SessionLog>>
}