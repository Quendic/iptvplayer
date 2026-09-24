package com.yunusemre.m3ustream.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSearchResponse(
    val page: Int,
    val results: List<TmdbMovie>,
    @SerialName("total_results") val totalResults: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class TmdbMovie(
    val id: Int,
    val title: String? = null,
    @SerialName("original_title") val originalTitle: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("genre_ids") val genreIds: List<Int>? = null
)
