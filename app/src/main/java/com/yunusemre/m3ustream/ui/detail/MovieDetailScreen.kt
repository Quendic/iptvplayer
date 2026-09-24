package com.yunusemre.m3ustream.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import com.yunusemre.m3ustream.ui.components.TvIconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.yunusemre.m3ustream.ui.navigation.Screen

@Composable
fun MovieDetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.movieState.collectAsState()
    val contentId = navController.currentBackStackEntry?.arguments?.getString("contentId")

    LaunchedEffect(contentId) {
        contentId?.let { viewModel.loadMovie(it) }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val content = state.content ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            AsyncImage(
                model = content.posterUrl,
                contentDescription = content.title,
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
                            startY = 400f,
                            endY = 1200f
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

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = content.groupTitle.ifEmpty { content.category },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                var isPlayFocused by remember { mutableStateOf(false) }
                val playScale by animateFloatAsState(
                    targetValue = if (isPlayFocused) 1.05f else 1f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "play_scale"
                )

                Button(
                    onClick = { navController.navigate(Screen.Player.createRoute(content.id)) },
                    modifier = Modifier
                        .weight(1f)
                        .scale(playScale)
                        .onFocusChanged { isPlayFocused = it.isFocused }
                        .focusable(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (state.progress != null && state.progress!!.progressPercent > 0) 
                            stringResource(R.string.resume) else stringResource(R.string.play),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                var isFavFocused by remember { mutableStateOf(false) }
                val favColor by animateColorAsState(
                    targetValue = if (isFavFocused || state.isFavorite) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "fav_color"
                )
                val favScale by animateFloatAsState(
                    targetValue = if (isFavFocused) 1.05f else 1f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "fav_scale"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .scale(favScale)
                        .onFocusChanged { isFavFocused = it.isFocused }
                        .clickable { viewModel.toggleMovieFavorite() }
                        .focusable(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = ButtonDefaults.shape
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorites),
                            tint = favColor
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.favorites),
                            fontWeight = FontWeight.Bold,
                            color = favColor
                        )
                    }
                }
            }
        }
    }
}
