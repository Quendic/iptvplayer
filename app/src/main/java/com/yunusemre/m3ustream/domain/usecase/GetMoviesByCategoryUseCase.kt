package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesByCategoryUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(category: String, limit: Int = 20): Flow<List<Content>> {
        return repository.getMoviesByCategory(category, limit)
    }
}
