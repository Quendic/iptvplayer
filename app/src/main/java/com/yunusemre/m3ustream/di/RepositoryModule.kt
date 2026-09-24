package com.yunusemre.m3ustream.di

import com.yunusemre.m3ustream.data.repository.ContentRepositoryImpl
import com.yunusemre.m3ustream.data.repository.FavoriteRepositoryImpl
import com.yunusemre.m3ustream.data.repository.MetadataRepositoryImpl
import com.yunusemre.m3ustream.data.repository.WatchHistoryRepositoryImpl
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.repository.FavoriteRepository
import com.yunusemre.m3ustream.domain.repository.MetadataRepository
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRepository(
        impl: ContentRepositoryImpl
    ): ContentRepository

    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(
        impl: WatchHistoryRepositoryImpl
    ): WatchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindMetadataRepository(
        impl: MetadataRepositoryImpl
    ): MetadataRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl
    ): FavoriteRepository
}
