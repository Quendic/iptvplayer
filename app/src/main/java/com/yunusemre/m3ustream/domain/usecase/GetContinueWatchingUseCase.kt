package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.WatchProgress
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetContinueWatchingUseCase @Inject constructor(
    private val watchHistoryRepository: WatchHistoryRepository,
    private val contentRepository: ContentRepository
) {
    operator fun invoke(): Flow<List<Pair<Content, WatchProgress>>> {
        return watchHistoryRepository.getContinueWatching().flatMapLatest { progresses ->
            if (progresses.isEmpty()) {
                flowOf(emptyList())
            } else {
                val contentFlows = progresses.map { progress ->
                    contentRepository.getContentById(progress.contentId).map { content ->
                        if (content != null) Pair(content, progress) else null
                    }
                }
                combine(contentFlows) { pairs ->
                    pairs.filterNotNull()
                }
            }
        }
    }
}
