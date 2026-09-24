package com.yunusemre.m3ustream.domain.repository

import com.yunusemre.m3ustream.domain.model.Content
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(): Flow<List<Content>>
    suspend fun addFavorite(content: Content)
    suspend fun removeFavorite(id: String)
    fun isFavorite(id: String): Flow<Boolean>
}
