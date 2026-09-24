package com.yunusemre.m3ustream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val id: String,  // MD5 of streamUrl
    val title: String,
    val originalName: String,
    val type: String,  // "movie" or "series"
    val category: String,
    val seriesName: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val streamUrl: String,
    val posterUrl: String?,  // from tvg-logo
    val tmdbPosterUrl: String?,
    val groupTitle: String,
    val parsedAt: Long
)
