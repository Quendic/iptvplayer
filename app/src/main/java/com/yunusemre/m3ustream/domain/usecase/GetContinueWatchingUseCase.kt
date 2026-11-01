package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.domain.model.WatchProgress
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
                    val validPairs = pairs.filterNotNull()
                    val seenSeries = mutableSetOf<String>()
                    val distinctList = mutableListOf<Pair<Content, WatchProgress>>()

                    for (pair in validPairs) {
                        val (content, _) = pair
                        val isSeries = content.type == ContentType.SERIES || !content.seriesName.isNullOrBlank()

                        if (isSeries) {
                            // Dizi anahtarını belirle (seriesName varsa onu, yoksa başlığı temizleyerek)
                            val rawSeriesName = content.seriesName?.takeIf { it.isNotBlank() }
                                ?: extractSeriesName(content.title)
                            val seriesKey = rawSeriesName.trim().lowercase()

                            if (seenSeries.add(seriesKey)) {
                                // progresses listesi son izlenme tarihine göre (lastWatchedAt DESC) sıralı olduğu için,
                                // bu dizinin en son izlenen bölümü ilk sırada gelir.
                                distinctList.add(pair)
                            }
                        } else {
                            // Film veya tekil içerik
                            distinctList.add(pair)
                        }
                    }

                    // En fazla 20 benzersiz içerik göster
                    distinctList.take(20)
                }
            }
        }
    }

    private fun extractSeriesName(title: String): String {
        val clean = title.replace(EPISODE_SUFFIX_REGEX, "").trim()
        return if (clean.isNotBlank()) clean else title
    }

    companion object {
        private val EPISODE_SUFFIX_REGEX = Regex(
            """(?:[\s._-]+|[\[(])(?:S\d{1,2}[\s._-]*E\d{1,3}|\d{1,2}\.\s*Sezon|\d{1,2}x\d{1,3}).*$""",
            RegexOption.IGNORE_CASE
        )
    }
}
