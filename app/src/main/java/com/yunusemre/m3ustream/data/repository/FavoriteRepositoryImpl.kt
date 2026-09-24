package com.yunusemre.m3ustream.data.repository

import com.yunusemre.m3ustream.data.local.db.FavoriteDao
import com.yunusemre.m3ustream.data.local.entity.FavoriteEntity
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.domain.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<Content>> {
        return favoriteDao.getFavorites().map { list ->
            list.map { entity ->
                Content(
                    id = entity.id,
                    title = entity.title,
                    originalName = entity.title,
                    type = if (entity.isSeries) ContentType.SERIES else ContentType.MOVIE,
                    category = entity.category,
                    seriesName = if (entity.isSeries) entity.title else null,
                    streamUrl = "",
                    posterUrl = entity.posterUrl,
                    groupTitle = entity.category
                )
            }
        }
    }

    override suspend fun addFavorite(content: Content) = withContext(Dispatchers.IO) {
        favoriteDao.insert(
            FavoriteEntity(
                id = content.id,
                isSeries = content.type == ContentType.SERIES,
                title = content.title,
                posterUrl = content.posterUrl,
                category = content.category,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO) {
        favoriteDao.delete(id)
    }

    override fun isFavorite(id: String): Flow<Boolean> {
        return favoriteDao.isFavorite(id)
    }
}
