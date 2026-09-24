package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getCategories()
    }
}
