package com.yunusemre.m3ustream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yunusemre.m3ustream.domain.model.ContentType

enum class DownloadState {
    QUEUED, DOWNLOADING, COMPLETED, PAUSED, ERROR, CANCELLED
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val type: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val localFilePath: String? = null,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val state: String = DownloadState.QUEUED.name,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
