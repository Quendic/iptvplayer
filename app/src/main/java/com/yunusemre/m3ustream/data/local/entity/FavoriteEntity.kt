package com.yunusemre.m3ustream.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite",
    indices = [Index(value = ["addedAt"])]
)
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val isSeries: Boolean = false,
    val title: String = "",
    val posterUrl: String? = null,
    val category: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
