package com.yunusemre.m3ustream.data.repository

import com.yunusemre.m3ustream.data.local.db.ContentDao
import com.yunusemre.m3ustream.data.remote.TmdbApiService
import com.yunusemre.m3ustream.domain.repository.MetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val tmdbApiService: TmdbApiService,
    private val contentDao: ContentDao
) : MetadataRepository {

    override suspend fun fetchAndCacheMoviePoster(title: String, year: Int?, contentId: String) = withContext(Dispatchers.IO) {
        try {
            val response = tmdbApiService.searchMovie(title, year)
            val posterPath = response.results.firstOrNull()?.posterPath
            if (posterPath != null) {
                val fullUrl = "${TmdbApiService.IMAGE_BASE_URL}w500$posterPath"
                contentDao.updateTmdbPoster(contentId, fullUrl)
            }
        } catch (_: Exception) {
            // Network error or rate limit
        }
    }

    override suspend fun fetchAndCacheSeriesPoster(seriesName: String) = withContext(Dispatchers.IO) {
        try {
            val response = tmdbApiService.searchTvShow(seriesName)
            val posterPath = response.results.firstOrNull()?.posterPath
            if (posterPath != null) {
                val fullUrl = "${TmdbApiService.IMAGE_BASE_URL}w500$posterPath"
                contentDao.updateSeriesTmdbPoster(seriesName, fullUrl)
            }
        } catch (_: Exception) {
            // Network error or rate limit
        }
    }

    override suspend fun syncAllMissingPosters() = withContext(Dispatchers.IO) {
        try {
            val missing = contentDao.getMissingPosters(20)
            for (item in missing) {
                if (item.type == "series" && item.seriesName != null) {
                    fetchAndCacheSeriesPoster(item.seriesName)
                } else {
                    fetchAndCacheMoviePoster(item.title, null, item.id)
                }
            }
        } catch (_: Exception) {
            // Network error or rate limit
        }
    }
}
