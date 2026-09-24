package com.yunusemre.m3ustream.domain.model

data class Episode(
    val id: String,
    val title: String,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val streamUrl: String,
    val posterUrl: String?
)
