package com.yunusemre.m3ustream.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yunusemre.m3ustream.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE contentId = :contentId")
    fun getWatchHistory(contentId: String): Flow<WatchHistoryEntity?>

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT 20")
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Upsert
    suspend fun upsert(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE contentId = :contentId")
    suspend fun delete(contentId: String)
}
