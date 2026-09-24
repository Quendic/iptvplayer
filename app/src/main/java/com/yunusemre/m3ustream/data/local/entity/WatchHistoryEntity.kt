package com.yunusemre.m3ustream.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_history",
    foreignKeys = [
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WatchHistoryEntity(
    @PrimaryKey val contentId: String,
    val lastPosition: Long,
    val totalDuration: Long,
    val progressPercent: Float,
    val lastWatchedAt: Long,
    val isCompleted: Boolean
)
