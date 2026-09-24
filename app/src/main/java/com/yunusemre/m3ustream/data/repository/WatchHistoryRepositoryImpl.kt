package com.yunusemre.m3ustream.data.repository

import com.yunusemre.m3ustream.data.local.db.WatchHistoryDao
import com.yunusemre.m3ustream.data.local.entity.WatchHistoryEntity
import com.yunusemre.m3ustream.domain.model.WatchProgress
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WatchHistoryRepositoryImpl @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : WatchHistoryRepository {

    override fun getWatchProgress(contentId: String): Flow<WatchProgress?> {
        return watchHistoryDao.getWatchHistory(contentId).map { it?.toDomain() }
    }

    override fun getContinueWatching(): Flow<List<WatchProgress>> {
        return watchHistoryDao.getContinueWatching().map { list -> list.map { it.toDomain() } }
    }

    override fun getAllHistory(): Flow<List<WatchProgress>> {
        return watchHistoryDao.getAllHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveProgress(contentId: String, position: Long, duration: Long) = withContext(Dispatchers.IO) {
        val percent = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
        val completed = percent >= 0.90f
        val entity = WatchHistoryEntity(
            contentId = contentId,
            lastPosition = position,
            totalDuration = duration,
            progressPercent = percent,
            lastWatchedAt = System.currentTimeMillis(),
            isCompleted = completed
        )
        watchHistoryDao.upsert(entity)
    }

    override suspend fun markCompleted(contentId: String) = withContext(Dispatchers.IO) {
        val entity = WatchHistoryEntity(
            contentId = contentId,
            lastPosition = 0L,
            totalDuration = 0L,
            progressPercent = 1.0f,
            lastWatchedAt = System.currentTimeMillis(),
            isCompleted = true
        )
        watchHistoryDao.upsert(entity)
    }

    override suspend fun deleteHistory(contentId: String) = withContext(Dispatchers.IO) {
        watchHistoryDao.delete(contentId)
    }

    private fun WatchHistoryEntity.toDomain(): WatchProgress = WatchProgress(
        contentId = contentId,
        lastPosition = lastPosition,
        totalDuration = totalDuration,
        progressPercent = progressPercent,
        lastWatchedAt = lastWatchedAt,
        isCompleted = isCompleted
    )
}
