package com.yunusemre.m3ustream.ui.search

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.ui.components.ContentCard
import com.yunusemre.m3ustream.ui.components.SearchBar
import com.yunusemre.m3ustream.ui.navigation.Screen
import java.util.Locale

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.updateQuery(spokenText)
            }
        }
    }

    val startVoiceRecognition = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_search_prompt))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            }
            voiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.voice_search_not_supported), Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val code = keyEvent.nativeKeyEvent.keyCode
                    if (code == KeyEvent.KEYCODE_SEARCH || code == KeyEvent.KEYCODE_VOICE_ASSIST) {
                        startVoiceRecognition()
                        return@onKeyEvent true
                    }
                }
                false
            }
    ) {
        SearchBar(
            query = uiState.query,
            onQueryChange = { viewModel.updateQuery(it) },
            onVoiceSearchClick = startVoiceRecognition
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.results.isEmpty() && uiState.query.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Sonuç bulunamadı", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.results) { content ->
                    ContentCard(
                        title = content.title,
                        posterUrl = content.posterUrl,
                        onClick = {
                            if (content.type == ContentType.MOVIE) {
                                navController.navigate(Screen.MovieDetail.createRoute(content.id))
                            } else {
                                navController.navigate(Screen.SeriesDetail.createRoute(content.seriesName ?: content.title))
                            }
                        }
                    )
                }
            }
        }
    }
}

