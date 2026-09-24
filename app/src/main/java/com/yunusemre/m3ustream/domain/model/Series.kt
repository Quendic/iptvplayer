package com.yunusemre.m3ustream.domain.model

data class Series(
    val name: String,
    val category: String,
    val posterUrl: String?,
    val totalSeasons: Int,
    val totalEpisodes: Int,
    val seasons: List<Season> = emptyList()
)
