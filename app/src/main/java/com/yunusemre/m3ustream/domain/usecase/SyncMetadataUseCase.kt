package com.yunusemre.m3ustream.domain.usecase

import com.yunusemre.m3ustream.domain.repository.MetadataRepository
import javax.inject.Inject

class SyncMetadataUseCase @Inject constructor(
    private val repository: MetadataRepository
) {
    suspend operator fun invoke() {
        repository.syncAllMissingPosters()
    }
}
