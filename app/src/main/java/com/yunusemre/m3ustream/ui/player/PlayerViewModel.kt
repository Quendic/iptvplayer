package com.yunusemre.m3ustream.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.domain.repository.ContentRepository
import com.yunusemre.m3ustream.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class AudioTrackInfo(
    val language: String,
    val label: String,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int
)

data class SubtitleTrackInfo(
    val language: String,
    val label: String,
    val isSelected: Boolean,
    val groupIndex: Int,
    val trackIndex: Int
)

data class PlayerUiState(
    val content: Content? = null,
    val nextEpisode: Content? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isControlsVisible: Boolean = true,
    val isLocked: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val audioTracks: List<AudioTrackInfo> = emptyList(),
    val subtitleTracks: List<SubtitleTrackInfo> = emptyList(),
    val subtitleSize: Float = 22f,
    val showTrackSelector: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    private val trackPreferences = TrackPreferences(context)

    fun loadContent(contentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val content = contentRepository.getContentById(contentId).firstOrNull()
            if (content != null) {
                _uiState.update { it.copy(content = content, isLoading = false) }
                val progress = watchHistoryRepository.getWatchProgress(contentId).firstOrNull()
                if (progress != null) {
                    _uiState.update { it.copy(currentPosition = progress.lastPosition) }
                }
                
                if (content.type == ContentType.SERIES && content.seriesName != null && content.seasonNumber != null && content.episodeNumber != null) {
                    val nextEps = contentRepository.getEpisodesBySeason(content.seriesName, content.seasonNumber).firstOrNull()
                    val next = nextEps?.find { it.episodeNumber == content.episodeNumber + 1 }
                    _uiState.update { it.copy(nextEpisode = next) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "İçerik bulunamadı") }
            }
        }
    }

    fun saveProgress(position: Long, duration: Long) {
        val contentId = _uiState.value.content?.id ?: return
        viewModelScope.launch {
            if (position > 0 && duration > 0) {
                watchHistoryRepository.saveProgress(contentId, position, duration)
            }
        }
    }

    fun updatePlaybackState(isPlaying: Boolean, currentPosition: Long, duration: Long) {
        _uiState.update {
            it.copy(
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration.coerceAtLeast(0)
            )
        }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error, isLoading = false) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun toggleControls() {
        if (!_uiState.value.isLocked) {
            _uiState.update { it.copy(isControlsVisible = !it.isControlsVisible) }
        } else {
            // When locked, show controls briefly to let user tap unlock
            _uiState.update { it.copy(isControlsVisible = true) }
        }
    }

    fun setControlsVisible(visible: Boolean) {
        if (!_uiState.value.isLocked) {
            _uiState.update { it.copy(isControlsVisible = visible) }
        }
    }

    fun toggleLock() {
        _uiState.update { it.copy(isLocked = !it.isLocked) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setShowTrackSelector(show: Boolean) {
        _uiState.update { it.copy(showTrackSelector = show) }
    }

    fun increaseSubtitleSize() {
        _uiState.update { it.copy(subtitleSize = (it.subtitleSize + 2f).coerceAtMost(36f)) }
    }

    fun decreaseSubtitleSize() {
        _uiState.update { it.copy(subtitleSize = (it.subtitleSize - 2f).coerceAtLeast(14f)) }
    }

    fun loadTracks(player: Player) {
        val rawAudioTracks = mutableListOf<AudioTrackInfo>()
        val rawSubtitleTracks = mutableListOf<SubtitleTrackInfo>()

        for (groupIndex in 0 until player.currentTracks.groups.size) {
            val group = player.currentTracks.groups[groupIndex]
            for (trackIndex in 0 until group.length) {
                val format = group.getTrackFormat(trackIndex)
                val isSelected = group.isTrackSelected(trackIndex)
                
                if (group.type == C.TRACK_TYPE_AUDIO) {
                    if (group.isTrackSupported(trackIndex)) {
                        val label = formatTrackTitle(format, isAudio = true, trackIndex = rawAudioTracks.size)
                        rawAudioTracks.add(
                            AudioTrackInfo(
                                language = format.language ?: "und",
                                label = label,
                                isSelected = isSelected,
                                groupIndex = groupIndex,
                                trackIndex = trackIndex
                            )
                        )
                    }
                } else if (group.type == C.TRACK_TYPE_TEXT) {
                    val label = formatTrackTitle(format, isAudio = false, trackIndex = rawSubtitleTracks.size)
                    rawSubtitleTracks.add(
                        SubtitleTrackInfo(
                            language = format.language ?: "und",
                            label = label,
                            isSelected = isSelected,
                            groupIndex = groupIndex,
                            trackIndex = trackIndex
                        )
                    )
                }
            }
        }

        // Deduplicate labels if identical (e.g. two tracks named "Türkçe")
        val audioTracks = deduplicateAudioLabels(rawAudioTracks)
        val subtitleTracks = deduplicateSubtitleLabels(rawSubtitleTracks)

        _uiState.update { it.copy(audioTracks = audioTracks, subtitleTracks = subtitleTracks) }
    }

    private fun formatTrackTitle(format: Format, isAudio: Boolean, trackIndex: Int): String {
        val langRaw = format.language?.lowercase()?.trim()

        val langName = when {
            langRaw == "tur" || langRaw == "tr" || langRaw == "turk" -> "Türkçe"
            langRaw == "eng" || langRaw == "en" -> "İngilizce"
            langRaw == "deu" || langRaw == "ger" || langRaw == "de" -> "Almanca"
            langRaw == "fra" || langRaw == "fre" || langRaw == "fr" -> "Fransızca"
            langRaw == "spa" || langRaw == "es" -> "İspanyolca"
            langRaw == "ita" || langRaw == "it" -> "İtalyanca"
            langRaw == "rus" || langRaw == "ru" -> "Rusça"
            langRaw == "ara" || langRaw == "ar" -> "Arapça"
            langRaw == "jpn" || langRaw == "ja" -> "Japonca"
            langRaw == "kor" || langRaw == "ko" -> "Korece"
            langRaw == "hin" || langRaw == "hi" -> "Hintçe"
            langRaw == "por" || langRaw == "pt" -> "Portekizce"
            !langRaw.isNullOrEmpty() && langRaw != "und" -> {
                try {
                    Locale.forLanguageTag(langRaw).getDisplayLanguage(Locale("tr")).ifEmpty {
                        Locale(langRaw).getDisplayLanguage(Locale("tr"))
                    }.replaceFirstChar { it.uppercase() }
                } catch (_: Exception) {
                    langRaw.uppercase()
                }
            }
            else -> null
        }

        val originalLabel = format.label?.trim() ?: ""

        // Filter out website promotions / domains (e.g. filmbol.org, webteizle.com, etc.)
        val webRegex = Regex("""(?i)\b([a-z0-9_\-]+\.(org|com|net|site|xyz|tv|cc|pw|me|biz|info|club|pro|vip|online|live|tv)|www\.[a-z0-9_\-]+\.[a-z]+|https?://\S+)\b""")
        val isPurePromo = originalLabel.matches(webRegex) || originalLabel.contains(Regex("""(?i)\b(filmbol|webteizle|hdfilm|dizipal|sezonlukdizi|fullhd)\b"""))
        val cleanedLabel = if (isPurePromo) "" else originalLabel.replace(webRegex, "").trim()

        val tags = mutableListOf<String>()

        val isForced = (format.selectionFlags and C.SELECTION_FLAG_FORCED) != 0 ||
                originalLabel.contains("forced", ignoreCase = true) ||
                originalLabel.contains("zorunlu", ignoreCase = true)

        val isDefault = (format.selectionFlags and C.SELECTION_FLAG_DEFAULT) != 0

        val isDub = (format.roleFlags and C.ROLE_FLAG_DUB) != 0 ||
                originalLabel.contains("dublaj", ignoreCase = true) ||
                originalLabel.contains("dub", ignoreCase = true)

        val isOriginal = originalLabel.contains("original", ignoreCase = true) ||
                originalLabel.contains("orijinal", ignoreCase = true)

        if (isAudio) {
            if (isDub) tags.add("Dublaj")
            if (isOriginal) tags.add("Orijinal")
            if (format.channelCount == 6) tags.add("5.1")
            else if (format.channelCount == 8) tags.add("7.1")
        } else {
            if (isForced) tags.add("Forced")
            else if (isDefault) tags.add("Varsayılan")
        }

        // Keep meaningful descriptive labels if not promo
        if (cleanedLabel.isNotEmpty()) {
            val extra = cleanedLabel
                .replace(Regex("""(?i)\b(forced|zorunlu|dublaj|dub|original|orijinal|türkçe|turkce|english|ingilizce)\b"""), "")
                .replace(Regex("""[()\[\]]"""), "")
                .trim()
            if (extra.isNotEmpty() && !tags.any { it.equals(extra, ignoreCase = true) }) {
                tags.add(extra)
            }
        }

        val mainTitle = when {
            langName != null -> langName
            cleanedLabel.isNotEmpty() -> cleanedLabel
            isAudio -> "Ses İzi ${trackIndex + 1}"
            else -> "Altyazı ${trackIndex + 1}"
        }

        return if (tags.isNotEmpty()) {
            "$mainTitle (${tags.distinct().joinToString(", ")})"
        } else {
            mainTitle
        }
    }

    private fun deduplicateAudioLabels(tracks: List<AudioTrackInfo>): List<AudioTrackInfo> {
        val counts = tracks.groupingBy { it.label }.eachCount()
        val seen = mutableMapOf<String, Int>()
        return tracks.map { track ->
            if ((counts[track.label] ?: 0) > 1) {
                val idx = (seen[track.label] ?: 0) + 1
                seen[track.label] = idx
                track.copy(label = "${track.label} #$idx")
            } else {
                track
            }
        }
    }

    private fun deduplicateSubtitleLabels(tracks: List<SubtitleTrackInfo>): List<SubtitleTrackInfo> {
        val counts = tracks.groupingBy { it.label }.eachCount()
        val seen = mutableMapOf<String, Int>()
        return tracks.map { track ->
            if ((counts[track.label] ?: 0) > 1) {
                val idx = (seen[track.label] ?: 0) + 1
                seen[track.label] = idx
                track.copy(label = "${track.label} #$idx")
            } else {
                track
            }
        }
    }

    fun selectAudioTrack(player: Player, groupIndex: Int, trackIndex: Int) {
        val group = player.currentTracks.groups[groupIndex]
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
            .build()
        
        viewModelScope.launch {
            val format = group.getTrackFormat(trackIndex)
            format.language?.let { trackPreferences.saveAudioLang(it) }
        }
        loadTracks(player)
    }

    fun selectSubtitleTrack(player: Player, groupIndex: Int, trackIndex: Int) {
        val group = player.currentTracks.groups[groupIndex]
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
            .build()
        
        viewModelScope.launch {
            val format = group.getTrackFormat(trackIndex)
            format.language?.let { trackPreferences.saveSubtitleLang(it) }
            trackPreferences.setSubtitleEnabled(true)
        }
        loadTracks(player)
    }

    fun disableSubtitles(player: Player) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .build()
        
        viewModelScope.launch {
            trackPreferences.setSubtitleEnabled(false)
        }
        loadTracks(player)
    }
    
    fun applyTrackPreferences(player: Player) {
        viewModelScope.launch {
            val audioLang = trackPreferences.audioLangFlow.firstOrNull()
            val subtitleLang = trackPreferences.subtitleLangFlow.firstOrNull()
            val subtitleEnabled = trackPreferences.subtitleEnabledFlow.firstOrNull() ?: false
            
            val parametersBuilder = player.trackSelectionParameters.buildUpon()
            
            if (audioLang != null) {
                parametersBuilder.setPreferredAudioLanguage(audioLang)
            }
            if (subtitleEnabled && subtitleLang != null) {
                parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                parametersBuilder.setPreferredTextLanguage(subtitleLang)
            } else if (!subtitleEnabled) {
                parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                parametersBuilder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
            }
            player.trackSelectionParameters = parametersBuilder.build()
        }
    }
}
