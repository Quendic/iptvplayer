package com.yunusemre.m3ustream.domain.model

data class Content(
    val id: String,
    val title: String,
    val originalName: String,
    val type: ContentType,
    val category: String,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val streamUrl: String,
    val posterUrl: String? = null,
    val groupTitle: String,
    val progressPercent: Float? = null,
    val lastPosition: Long? = null
)

enum class ContentType { MOVIE, SERIES }
