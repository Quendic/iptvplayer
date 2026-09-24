package com.yunusemre.m3ustream.domain.repository

import com.yunusemre.m3ustream.domain.model.WatchProgress
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun getWatchProgress(contentId: String): Flow<WatchProgress?>
    fun getContinueWatching(): Flow<List<WatchProgress>>
    fun getAllHistory(): Flow<List<WatchProgress>>
    suspend fun saveProgress(contentId: String, position: Long, duration: Long)
    suspend fun markCompleted(contentId: String)
    suspend fun deleteHistory(contentId: String)
}
