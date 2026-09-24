package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class SaveWatchProgressUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(contentId: String, position: Long, duration: Long) {
        if (duration <= 0) return
        
        repository.saveProgress(contentId, position, duration)
        
        val progressPercent = (position.toFloat() / duration.toFloat()) * 100f
        if (progressPercent > 90f) {
            repository.markCompleted(contentId)
        }
    }
}
