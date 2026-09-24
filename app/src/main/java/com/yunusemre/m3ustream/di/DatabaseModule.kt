package com.yunusemre.m3ustream.di

import android.content.Context
import androidx.room.Room
import com.yunusemre.m3ustream.data.local.db.AppDatabase
import com.yunusemre.m3ustream.data.local.db.ContentDao
import com.yunusemre.m3ustream.data.local.db.FavoriteDao
import com.yunusemre.m3ustream.data.local.db.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "m3ustream_db"
        )
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    @Provides
    fun provideContentDao(database: AppDatabase): ContentDao = database.contentDao()

    @Provides
    fun provideWatchHistoryDao(database: AppDatabase): WatchHistoryDao = database.watchHistoryDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()
}
