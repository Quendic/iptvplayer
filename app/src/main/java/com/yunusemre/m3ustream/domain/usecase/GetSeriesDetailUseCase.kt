package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.Series
import com.yunusemre.m3ustream.domain.model.Season
import com.yunusemre.m3ustream.domain.model.Episode
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSeriesDetailUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(seriesName: String): Flow<Series?> {
        return repository.getEpisodesBySeries(seriesName).map { episodesContent ->
            if (episodesContent.isEmpty()) return@map null
            
            val first = episodesContent.first()
            
            val seasonsList = episodesContent
                .groupBy { it.seasonNumber ?: 1 }
                .map { (seasonNum, contents) ->
                    Season(
                        number = seasonNum,
                        episodes = contents.map { content ->
                            Episode(
                                id = content.id,
                                title = content.title,
                                episodeNumber = content.episodeNumber ?: 0,
                                seasonNumber = seasonNum,
                                streamUrl = content.streamUrl,
                                posterUrl = content.posterUrl
                            )
                        }.sortedBy { it.episodeNumber }
                    )
                }
                .sortedBy { it.number }

            Series(
                name = seriesName,
                category = first.category,
                posterUrl = first.posterUrl,
                totalSeasons = seasonsList.size,
                totalEpisodes = episodesContent.size,
                seasons = seasonsList
            )
        }
    }
}
