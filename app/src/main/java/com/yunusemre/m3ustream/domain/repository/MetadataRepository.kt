package com.yunusemre.m3ustream.domain.repository

interface MetadataRepository {
    suspend fun fetchAndCacheMoviePoster(title: String, year: Int?, contentId: String)
    suspend fun fetchAndCacheSeriesPoster(seriesName: String)
    suspend fun syncAllMissingPosters()
}
