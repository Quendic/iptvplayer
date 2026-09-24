package com.yunusemre.m3ustream.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.usecase.GetContinueWatchingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.combine
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository

data class HomeUiState(
    val continueWatching: List<Content> = emptyList(),
    val categories: Map<String, List<Content>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val needsSetup: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            contentRepository.getContentCount().collect { count ->
                _uiState.update { it.copy(needsSetup = count == 0) }
            }
        }

        viewModelScope.launch {
            getContinueWatchingUseCase().collect { pairs ->
                val list = pairs.map { (content, progress) ->
                    content.copy(
                        progressPercent = progress.progressPercent,
                        lastPosition = progress.lastPosition
                    )
                }
                _uiState.update { it.copy(continueWatching = list) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                contentRepository.getSeriesGroups(),
                contentRepository.getAllMovies(30),
                contentRepository.getCategories()
            ) { seriesList, moviesList, categoryList ->
                val categoryMap = LinkedHashMap<String, List<Content>>()

                if (seriesList.isNotEmpty()) {
                    categoryMap["📺 Popüler Diziler"] = seriesList.map { s ->
                        Content(
                            id = s.name,
                            title = s.name,
                            originalName = s.name,
                            type = ContentType.SERIES,
                            category = s.category,
                            seriesName = s.name,
                            streamUrl = "",
                            posterUrl = s.posterUrl,
                            groupTitle = s.category
                        )
                    }
                }

                if (moviesList.isNotEmpty()) {
                    categoryMap["🎬 Popüler Filmler"] = moviesList
                }

                for (cat in categoryList.take(20)) {
                    if (cat.isBlank()) continue
                    val items = contentRepository.getCategoryContents(cat, 15).firstOrNull() ?: emptyList()
                    if (items.isNotEmpty()) {
                        categoryMap[cat] = items
                    }
                }

                categoryMap
            }.collect { map ->
                _uiState.update { it.copy(categories = map, isLoading = false) }
            }
        }
    }

    fun removeFromHistory(contentId: String) {
        viewModelScope.launch {
            watchHistoryRepository.deleteHistory(contentId)
        }
    }
}
