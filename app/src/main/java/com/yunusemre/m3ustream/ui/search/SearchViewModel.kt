package com.yunusemre.m3ustream.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.usecase.SearchContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Content> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchContentUseCase: SearchContentUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    if (q.length < 2) {
                        _uiState.update { it.copy(isLoading = false, results = emptyList()) }
                        flowOf(emptyList())
                    } else {
                        _uiState.update { it.copy(isLoading = true) }
                        searchContentUseCase(q)
                    }
                }
                .collect { list ->
                    _uiState.update { it.copy(results = list, isLoading = false) }
                }
        }
    }

    fun updateQuery(query: String) {
        _query.value = query
        _uiState.update { it.copy(query = query) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }
}
