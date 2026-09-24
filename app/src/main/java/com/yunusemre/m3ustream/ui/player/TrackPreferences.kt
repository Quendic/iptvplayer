package com.yunusemre.m3ustream.ui.player

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "track_prefs")

class TrackPreferences(private val context: Context) {
    companion object {
        val AUDIO_LANG = stringPreferencesKey("audio_lang")
        val SUBTITLE_LANG = stringPreferencesKey("subtitle_lang")
        val SUBTITLE_ENABLED = booleanPreferencesKey("subtitle_enabled")
    }

    val audioLangFlow: Flow<String?> = context.dataStore.data.map { it[AUDIO_LANG] }
    val subtitleLangFlow: Flow<String?> = context.dataStore.data.map { it[SUBTITLE_LANG] }
    val subtitleEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[SUBTITLE_ENABLED] ?: false }

    suspend fun saveAudioLang(lang: String) {
        context.dataStore.edit { it[AUDIO_LANG] = lang }
    }

    suspend fun saveSubtitleLang(lang: String) {
        context.dataStore.edit { it[SUBTITLE_LANG] = lang }
    }

    suspend fun setSubtitleEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SUBTITLE_ENABLED] = enabled }
    }
}
