package com.yunusemre.m3ustream.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yunusemre.m3ustream.data.local.entity.ContentEntity
import com.yunusemre.m3ustream.data.local.entity.FavoriteEntity
import com.yunusemre.m3ustream.data.local.entity.SeriesGroupEntity
import com.yunusemre.m3ustream.data.local.entity.WatchHistoryEntity

@Database(
    entities = [
        ContentEntity::class,
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        SeriesGroupEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
}
