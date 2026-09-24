package com.yunusemre.m3ustream.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunusemre.m3ustream.domain.model.M3uSource
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.usecase.ParseM3uUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val m3uUrl: String = "",
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val movieCount: Int = 0,
    val seriesCount: Int = 0,
    val totalCount: Int = 0,
    val lastUpdate: String = "Henüz yapılmadı",
    val isParsing: Boolean = false,
    val statusMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val parseM3uUseCase: ParseM3uUseCase,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeCounts()
    }

    private fun observeCounts() {
        viewModelScope.launch {
            contentRepository.getContentCount().collect { count ->
                _uiState.update { it.copy(totalCount = count) }
            }
        }
        viewModelScope.launch {
            contentRepository.getMovieCount().collect { count ->
                _uiState.update { it.copy(movieCount = count) }
            }
        }
        viewModelScope.launch {
            contentRepository.getSeriesCount().collect { count ->
                _uiState.update { it.copy(seriesCount = count) }
            }
        }
    }

    fun updateM3uUrl(url: String) {
        _uiState.update { it.copy(m3uUrl = url, selectedFileUri = null, selectedFileName = null, error = null) }
    }

    fun selectFile(uri: Uri, fileName: String) {
        _uiState.update {
            it.copy(
                selectedFileUri = uri,
                selectedFileName = fileName,
                m3uUrl = "",
                error = null,
                statusMessage = "Dosya seçildi: $fileName"
            )
        }
    }

    fun importM3u() {
        val currentState = _uiState.value
        val source = when {
            currentState.selectedFileUri != null -> M3uSource.File(currentState.selectedFileUri)
            currentState.m3uUrl.isNotBlank() -> M3uSource.Url(currentState.m3uUrl.trim())
            else -> {
                _uiState.update { it.copy(error = "Lütfen bir M3U bağlantısı girin veya cihazınızdan dosya seçin.") }
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isParsing = true,
                    error = null,
                    statusMessage = "M3U listesi ayrıştırılıyor ve veritabanına kaydediliyor... Bu işlem listenin boyutuna göre birkaç saniye sürebilir."
                )
            }
            try {
                parseM3uUseCase(source)
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val now = sdf.format(Date())
                _uiState.update {
                    it.copy(
                        isParsing = false,
                        lastUpdate = now,
                        statusMessage = "Başarıyla içe aktarıldı! İçerikleriniz hazır."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isParsing = false,
                        error = "İçe aktarma hatası: ${e.localizedMessage ?: e.message ?: "Bilinmeyen hata"}"
                    )
                }
            }
        }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contentRepository.clearAll()
                _uiState.update {
                    it.copy(
                        selectedFileUri = null,
                        selectedFileName = null,
                        m3uUrl = "",
                        statusMessage = "Tüm veriler başarıyla temizlendi.",
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Temizleme hatası: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null, error = null) }
    }
}
