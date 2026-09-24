package com.yunusemre.m3ustream.data.remote

import com.yunusemre.m3ustream.data.remote.dto.TmdbSearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class TmdbApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    }

    suspend fun searchMovie(query: String, year: Int? = null): TmdbSearchResponse {
        return client.get("$BASE_URL/search/movie") {
            parameter("query", query)
            year?.let { parameter("primary_release_year", it) }
        }.body()
    }

    suspend fun searchTvShow(query: String): TmdbSearchResponse {
        return client.get("$BASE_URL/search/tv") {
            parameter("query", query)
        }.body()
    }
}
