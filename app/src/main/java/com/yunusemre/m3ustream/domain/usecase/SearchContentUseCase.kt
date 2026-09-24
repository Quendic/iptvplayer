package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchContentUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(query: String): Flow<List<Content>> {
        if (query.length < 2) {
            return flowOf(emptyList())
        }
        return repository.searchContent(query)
    }
}
