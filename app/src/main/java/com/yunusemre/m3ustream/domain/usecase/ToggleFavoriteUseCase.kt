package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(content: Content) {
        val isFav = repository.isFavorite(content.id).first()
        if (isFav) {
            repository.removeFavorite(content.id)
        } else {
            repository.addFavorite(content)
        }
    }
}
