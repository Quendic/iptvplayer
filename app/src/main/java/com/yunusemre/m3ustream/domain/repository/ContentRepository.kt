package com.yunusemre.m3ustream.domain.repository

import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.M3uSource
import com.yunusemre.m3ustream.domain.model.Series
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    suspend fun parseAndCacheM3u(source: M3uSource)
    fun getContentCount(): Flow<Int>
    fun getMovieCount(): Flow<Int>
    fun getSeriesCount(): Flow<Int>
    fun getCategories(): Flow<List<String>>
    fun getMoviesByCategory(category: String, limit: Int = 20): Flow<List<Content>>
    fun getAllMovies(limit: Int = 30): Flow<List<Content>>
    fun getCategoryContents(category: String, limit: Int = 15): Flow<List<Content>>
    fun getSeriesGroups(): Flow<List<Series>>
    fun getEpisodesBySeries(seriesName: String): Flow<List<Content>>
    fun getEpisodesBySeason(seriesName: String, season: Int): Flow<List<Content>>
    fun getContentById(id: String): Flow<Content?>
    fun searchContent(query: String): Flow<List<Content>>
    suspend fun clearAll()
}
