package com.yunusemre.m3ustream.domain.model

data class WatchProgress(
    val contentId: String,
    val lastPosition: Long,
    val totalDuration: Long,
    val progressPercent: Float,
    val lastWatchedAt: Long,
    val isCompleted: Boolean
)
