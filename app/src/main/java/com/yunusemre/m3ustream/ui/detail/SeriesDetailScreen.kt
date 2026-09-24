package com.yunusemre.m3ustream.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import com.yunusemre.m3ustream.ui.components.TvIconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.ui.components.EpisodeListItem
import com.yunusemre.m3ustream.ui.components.tvFocusable
import com.yunusemre.m3ustream.ui.navigation.Screen

@Composable
fun SeriesDetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.seriesState.collectAsState()
    val seriesName = navController.currentBackStackEntry?.arguments?.getString("seriesName")

    LaunchedEffect(seriesName) {
        seriesName?.let { viewModel.loadSeries(it) }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val series = state.series ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = series.posterUrl,
                    contentDescription = series.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                ),
                                startY = 300f,
                                endY = 900f
                            )
                        )
                )

                TvIconButton(
                    onClick = { navController.navigateUp() },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = series.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    TvIconButton(
                        onClick = { viewModel.toggleSeriesFavorite() },
                        icon = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorites),
                        normalTint = if (state.isFavorite) Color(0xFFE50914) else Color.White.copy(alpha = 0.85f),
                        focusedTint = Color(0xFFE50914)
                    )
                }

                Text(
                    text = "${series.totalSeasons} Sezon • ${series.totalEpisodes} Bölüm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Modern Yatay Sezon Seçici (Pill Tabs)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    items(series.seasons) { season ->
                        val isSelected = season.number == state.selectedSeason?.number
                        var isTabFocused by remember { mutableStateOf(false) }
                        val textColor by animateColorAsState(
                            targetValue = if (isTabFocused || isSelected) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "season_tab_text_color"
                        )
                        val tabScale by animateFloatAsState(
                            targetValue = if (isTabFocused) 1.06f else 1f,
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "season_tab_scale"
                        )
                        Surface(
                            modifier = Modifier
                                .scale(tabScale)
                                .onFocusChanged { isTabFocused = it.isFocused }
                                .clickable { viewModel.selectSeason(season) }
                                .focusable(),
                            color = if (isSelected) Color(0xFFE50914).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = String.format(stringResource(R.string.season_format), season.number),
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        state.selectedSeason?.let { season ->
            items(season.episodes) { episode ->
                EpisodeListItem(
                    episode = episode,
                    progress = state.progressMap[episode.id],
                    onClick = { navController.navigate(Screen.Player.createRoute(episode.id)) }
                )
            }
        }
    }
}
