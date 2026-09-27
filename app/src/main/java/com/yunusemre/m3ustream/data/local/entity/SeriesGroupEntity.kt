package com.yunusemre.m3ustream.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "series_group",
    indices = [Index(value = ["category"])]
)
data class SeriesGroupEntity(
    @PrimaryKey val seriesName: String,
    val category: String,
    val posterUrl: String?,
    val tmdbPosterUrl: String?,
    val totalSeasons: Int,
    val totalEpisodes: Int
)
