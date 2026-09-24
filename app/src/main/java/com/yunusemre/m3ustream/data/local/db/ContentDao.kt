package com.yunusemre.m3ustream.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.yunusemre.m3ustream.data.local.entity.ContentEntity
import com.yunusemre.m3ustream.data.local.entity.SeriesGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM content WHERE category = :category AND type = 'movie'")
    fun getMoviesByCategory(category: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM series_group")
    fun getSeriesGroups(): Flow<List<SeriesGroupEntity>>

    @Query("SELECT * FROM content WHERE seriesName = :seriesName AND type = 'series' ORDER BY seasonNumber, episodeNumber")
    fun getEpisodesBySeries(seriesName: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE seriesName = :seriesName AND seasonNumber = :season AND type = 'series' ORDER BY episodeNumber")
    fun getEpisodesBySeason(seriesName: String, season: Int): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE title LIKE '%' || :query || '%' OR originalName LIKE '%' || :query || '%'")
    fun searchContent(query: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE id = :id")
    fun getContentById(id: String): Flow<ContentEntity?>

    @Query("SELECT COUNT(*) FROM content")
    fun getContentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM content WHERE type = 'movie'")
    fun getMovieCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM series_group")
    fun getSeriesGroupCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM content")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM content WHERE category = :category AND type = 'movie' LIMIT :limit")
    fun getMoviesForCategory(category: String, limit: Int): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE type = 'movie' LIMIT :limit")
    fun getAllMovies(limit: Int): Flow<List<ContentEntity>>

    @Query("SELECT * FROM series_group WHERE category = :category LIMIT :limit")
    fun getSeriesForCategory(category: String, limit: Int): Flow<List<SeriesGroupEntity>>

    @Query("SELECT * FROM content WHERE category = :category LIMIT :limit")
    fun getContentForCategory(category: String, limit: Int): Flow<List<ContentEntity>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<ContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSeriesGroups(groups: List<SeriesGroupEntity>)

    @Query("DELETE FROM content")
    fun deleteAll()

    @Query("DELETE FROM series_group")
    fun deleteAllSeriesGroups()

    @Query("UPDATE content SET tmdbPosterUrl = :posterUrl WHERE id = :id")
    suspend fun updateTmdbPoster(id: String, posterUrl: String)

    @Query("UPDATE series_group SET tmdbPosterUrl = :posterUrl WHERE seriesName = :seriesName")
    suspend fun updateSeriesTmdbPoster(seriesName: String, posterUrl: String)

    @Query("SELECT * FROM content WHERE tmdbPosterUrl IS NULL AND (posterUrl IS NULL OR posterUrl = '') LIMIT :limit")
    suspend fun getMissingPosters(limit: Int = 50): List<ContentEntity>
}
