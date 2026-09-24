package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.model.M3uSource
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import javax.inject.Inject

class ParseM3uUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(source: M3uSource) {
        repository.parseAndCacheM3u(source)
    }
}
