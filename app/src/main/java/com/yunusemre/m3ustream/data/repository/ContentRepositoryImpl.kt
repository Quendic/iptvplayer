package com.yunusemre.m3ustream.data.repository

import android.content.Context
import com.yunusemre.m3ustream.data.local.db.ContentDao
import com.yunusemre.m3ustream.data.local.entity.ContentEntity
import com.yunusemre.m3ustream.data.local.entity.SeriesGroupEntity
import com.yunusemre.m3ustream.data.parser.ContentClassifier
import com.yunusemre.m3ustream.data.parser.M3uParser
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.domain.model.M3uSource
import com.yunusemre.m3ustream.domain.model.Series
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentDao: ContentDao,
    private val m3uParser: M3uParser,
    private val contentClassifier: ContentClassifier
) : ContentRepository {

    override suspend fun parseAndCacheM3u(source: M3uSource) = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = when (source) {
            is M3uSource.Url -> {
                try {
                    val url = URL(source.url)
                    val connection = url.openConnection()
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    connection.getInputStream()
                } catch (e: javax.net.ssl.SSLException) {
                    if (source.url.startsWith("https://", ignoreCase = true)) {
                        val fallbackUrl = source.url.replaceFirst("https://", "http://", ignoreCase = true)
                        val url = URL(fallbackUrl)
                        val connection = url.openConnection()
                        connection.connectTimeout = 15000
                        connection.readTimeout = 30000
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        connection.getInputStream()
                    } else {
                        throw e
                    }
                }
            }
            is M3uSource.File -> context.contentResolver.openInputStream(source.uri)
        }

        inputStream?.use { stream ->
            val items = m3uParser.parse(stream)
            val (contents, seriesGroups) = contentClassifier.classify(items)

            contentDao.deleteAll()
            contentDao.deleteAllSeriesGroups()

            contentDao.insertSeriesGroups(seriesGroups)
            contentDao.insertAll(contents)
        } ?: throw IllegalArgumentException("M3U kaynağı okunamadı")
    }

    override fun getContentCount(): Flow<Int> = contentDao.getContentCount()

    override fun getMovieCount(): Flow<Int> = contentDao.getMovieCount()

    override fun getSeriesCount(): Flow<Int> = contentDao.getSeriesGroupCount()

    override fun getCategories(): Flow<List<String>> = contentDao.getAllCategories()

    override fun getMoviesByCategory(category: String, limit: Int): Flow<List<Content>> {
        return contentDao.getMoviesForCategory(category, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllMovies(limit: Int): Flow<List<Content>> {
        return contentDao.getAllMovies(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getCategoryContents(category: String, limit: Int): Flow<List<Content>> {
        return contentDao.getContentForCategory(category, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSeriesGroups(): Flow<List<Series>> {
        return contentDao.getSeriesGroups().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getEpisodesBySeries(seriesName: String): Flow<List<Content>> {
        return contentDao.getEpisodesBySeries(seriesName).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getEpisodesBySeason(seriesName: String, season: Int): Flow<List<Content>> {
        return contentDao.getEpisodesBySeason(seriesName, season).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getContentById(id: String): Flow<Content?> {
        return contentDao.getContentById(id).map { it?.toDomain() }
    }

    override fun searchContent(query: String): Flow<List<Content>> {
        return contentDao.searchContent(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        contentDao.deleteAll()
        contentDao.deleteAllSeriesGroups()
    }

    private fun ContentEntity.toDomain(): Content = Content(
        id = id,
        title = title,
        originalName = originalName,
        type = if (type == "series") ContentType.SERIES else ContentType.MOVIE,
        category = category,
        seriesName = seriesName,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        streamUrl = streamUrl,
        posterUrl = posterUrl ?: tmdbPosterUrl,
        groupTitle = groupTitle
    )

    private fun SeriesGroupEntity.toDomain(): Series = Series(
        name = seriesName,
        category = category,
        posterUrl = posterUrl ?: tmdbPosterUrl,
        totalSeasons = totalSeasons,
        totalEpisodes = totalEpisodes,
        seasons = emptyList()
    )
}
