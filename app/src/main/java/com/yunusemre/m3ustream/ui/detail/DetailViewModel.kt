package com.yunusemre.m3ustream.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.domain.model.Season
import com.yunusemre.m3ustream.domain.model.Series
import com.yunusemre.m3ustream.domain.model.WatchProgress
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.repository.FavoriteRepository
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import com.yunusemre.m3ustream.domain.usecase.GetSeriesDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import javax.inject.Inject

import com.yunusemre.m3ustream.data.local.entity.DownloadEntity
import com.yunusemre.m3ustream.data.local.entity.DownloadState
import com.yunusemre.m3ustream.domain.repository.DownloadRepository
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

@Immutable
data class MovieDetailState(
    val content: Content? = null,
    val progress: WatchProgress? = null,
    val isFavorite: Boolean = false,
    val downloadEntity: DownloadEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Immutable
data class SeriesDetailState(
    val series: Series? = null,
    val selectedSeason: Season? = null,
    val progressMap: Map<String, WatchProgress> = emptyMap(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val downloadRepository: DownloadRepository,
    private val getSeriesDetailUseCase: GetSeriesDetailUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _movieState = MutableStateFlow(MovieDetailState())
    val movieState: StateFlow<MovieDetailState> = _movieState.asStateFlow()

    private val _seriesState = MutableStateFlow(SeriesDetailState())
    val seriesState: StateFlow<SeriesDetailState> = _seriesState.asStateFlow()

    fun loadMovie(contentId: String) {
        viewModelScope.launch {
            _movieState.update { it.copy(isLoading = true, error = null) }
            val content = contentRepository.getContentById(contentId).firstOrNull()
            if (content != null) {
                val progress = watchHistoryRepository.getWatchProgress(contentId).firstOrNull()
                val isFav = favoriteRepository.isFavorite(contentId).firstOrNull() ?: false
                _movieState.update {
                    it.copy(content = content, progress = progress, isFavorite = isFav, isLoading = false)
                }

                // Download durumunu dinle
                downloadRepository.getDownloadFlow(contentId).collect { download ->
                    _movieState.update { it.copy(downloadEntity = download) }
                }
            } else {
                _movieState.update { it.copy(isLoading = false, error = "Film bulunamadı") }
            }
        }
    }

    fun startDownload(content: Content) {
        viewModelScope.launch {
            val download = DownloadEntity(
                contentId = content.id,
                title = content.title,
                type = content.type.name,
                streamUrl = content.streamUrl,
                logoUrl = content.posterUrl,
                state = DownloadState.QUEUED.name
            )
            downloadRepository.addOrUpdateDownload(download)

            val workRequest = OneTimeWorkRequestBuilder<com.yunusemre.m3ustream.worker.VideoDownloadWorker>()
                .setInputData(
                    workDataOf(
                        com.yunusemre.m3ustream.worker.VideoDownloadWorker.KEY_CONTENT_ID to content.id,
                        com.yunusemre.m3ustream.worker.VideoDownloadWorker.KEY_STREAM_URL to content.streamUrl
                    )
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    fun loadSeries(seriesName: String) {
        viewModelScope.launch {
            _seriesState.update { it.copy(isLoading = true, error = null) }
            getSeriesDetailUseCase(seriesName).collect { series ->
                if (series != null) {
                    val defaultSeason = series.seasons.firstOrNull()
                    val isFav = favoriteRepository.isFavorite(series.name).firstOrNull() ?: false
                    _seriesState.update {
                        it.copy(
                            series = series,
                            selectedSeason = defaultSeason,
                            isFavorite = isFav,
                            isLoading = false
                        )
                    }
                } else {
                    _seriesState.update { it.copy(isLoading = false, error = "Dizi bulunamadı") }
                }
            }
        }
    }

    fun selectSeason(season: Season) {
        _seriesState.update { it.copy(selectedSeason = season) }
    }

    fun toggleMovieFavorite() {
        val content = _movieState.value.content ?: return
        viewModelScope.launch {
            val current = _movieState.value.isFavorite
            if (current) {
                favoriteRepository.removeFavorite(content.id)
            } else {
                favoriteRepository.addFavorite(content)
            }
            _movieState.update { it.copy(isFavorite = !current) }
        }
    }

    fun toggleSeriesFavorite() {
        val series = _seriesState.value.series ?: return
        viewModelScope.launch {
            val current = _seriesState.value.isFavorite
            if (current) {
                favoriteRepository.removeFavorite(series.name)
            } else {
                val seriesContent = Content(
                    id = series.name,
                    title = series.name,
                    originalName = series.name,
                    type = ContentType.SERIES,
                    category = series.category,
                    seriesName = series.name,
                    streamUrl = "",
                    posterUrl = series.posterUrl,
                    groupTitle = series.category
                )
                favoriteRepository.addFavorite(seriesContent)
            }
            _seriesState.update { it.copy(isFavorite = !current) }
        }
    }
}
