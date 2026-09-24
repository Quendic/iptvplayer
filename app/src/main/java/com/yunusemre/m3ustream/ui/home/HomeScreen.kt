package com.yunusemre.m3ustream.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.yunusemre.m3ustream.ui.components.TvIconButton
import com.yunusemre.m3ustream.ui.components.tvFocusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.domain.model.Content
import com.yunusemre.m3ustream.domain.model.ContentType
import com.yunusemre.m3ustream.ui.components.ContentRow
import com.yunusemre.m3ustream.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TvIconButton(
                        onClick = { navController.navigate(Screen.Search.route) },
                        icon = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TvIconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        icon = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.continueWatching.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.continueWatching.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Henüz İzlenen İçerik Yok",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Film veya dizi izlemeye başladığınızda kaldığınız yer ve bölüm bilgileri burada listelenecektir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    var isSearchBtnFocused by remember { mutableStateOf(false) }
                    val searchBtnContentColor by animateColorAsState(
                        targetValue = if (isSearchBtnFocused) Color(0xFFE50914) else Color.White,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "search_btn_color"
                    )
                    val searchBtnScale by animateFloatAsState(
                        targetValue = if (isSearchBtnFocused) 1.05f else 1f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "search_btn_scale"
                    )
                    Surface(
                        modifier = Modifier
                            .scale(searchBtnScale)
                            .onFocusChanged { isSearchBtnFocused = it.isFocused }
                            .clickable { navController.navigate(Screen.Search.route) }
                            .focusable(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = searchBtnContentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "İçerik Ara ve İzle",
                                fontWeight = FontWeight.Bold,
                                color = searchBtnContentColor
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Son İzlediklerim",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )
                        
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(uiState.continueWatching) { content ->
                                Column(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .tvFocusable(
                                            onClick = { navController.navigate(Screen.Player.createRoute(content.id)) },
                                            focusedBorderWidth = 2.dp,
                                            focusedScale = 1.05f,
                                            cornerRadius = 10.dp
                                        )
                                        .padding(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(2f / 3f)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = content.posterUrl,
                                            contentDescription = content.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        // Progress bar at bottom of poster
                                        val percent = content.progressPercent ?: 0f
                                        LinearProgressIndicator(
                                            progress = { percent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                                .height(4.dp),
                                            color = androidx.compose.ui.graphics.Color(0xFFE50914), // Netflix Red
                                            trackColor = androidx.compose.ui.graphics.Color.DarkGray.copy(alpha = 0.8f)
                                        )
                                        
                                        // Play icon overlay
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = "Play",
                                                tint = androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }

                                        // Close icon to remove from history
                                        androidx.compose.material3.IconButton(
                                            onClick = { viewModel.removeFromHistory(content.id) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .padding(4.dp)
                                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = content.seriesName ?: content.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    if (content.type == ContentType.SERIES) {
                                        Text(
                                            text = "S${content.seasonNumber} E${content.episodeNumber}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

private fun navigateToDetail(navController: NavController, content: Content) {
    if (content.type == ContentType.MOVIE) {
        navController.navigate(Screen.MovieDetail.createRoute(content.id))
    } else {
        navController.navigate(Screen.SeriesDetail.createRoute(content.seriesName ?: content.title))
    }
}
