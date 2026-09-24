package com.yunusemre.m3ustream.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favoriteRepository: FavoriteRepository
) : ViewModel() {
    val favorites: StateFlow<List<Content>> = favoriteRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
