package com.yunusemre.m3ustream.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.ExperimentalComposeUiApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TrackSelectorSheet(
    audioTracks: List<AudioTrackInfo>,
    subtitleTracks: List<SubtitleTrackInfo>,
    onAudioTrackSelected: (Int, Int) -> Unit,
    onSubtitleTrackSelected: (Int, Int) -> Unit,
    onSubtitleDisabled: () -> Unit,
    subtitleSize: Float,
    onSubtitleSizeIncrease: () -> Unit,
    onSubtitleSizeDecrease: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isTv = remember(context) {
        val uiModeManager = context.getSystemService(android.content.Context.UI_MODE_SERVICE) as? android.app.UiModeManager
        uiModeManager?.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
    }

    val panelFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (isTv) {
            try {
                panelFocusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    val sheetContent = @Composable {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Audio Tracks Section
            item {
                Text(
                    text = "🗣️ Ses İzi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (audioTracks.isEmpty()) {
                item {
                    Text(
                        text = "Ses izi bulunamadı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                items(audioTracks.size, key = { "audio_$it" }) { index ->
                    val track = audioTracks[index]
                    TrackSelectorRow(
                        label = track.label,
                        isSelected = track.isSelected,
                        modifier = if (index == 0) Modifier.focusRequester(panelFocusRequester) else Modifier,
                        onClick = { onAudioTrackSelected(track.groupIndex, track.trackIndex) }
                    )
                }
                item { 
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Subtitles Section
            item {
                Text(
                    text = "📝 Altyazı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                val isOffSelected = subtitleTracks.none { it.isSelected }
                TrackSelectorRow(
                    label = "Kapalı",
                    isSelected = isOffSelected,
                    modifier = if (audioTracks.isEmpty()) Modifier.focusRequester(panelFocusRequester) else Modifier,
                    onClick = onSubtitleDisabled
                )
            }
            items(subtitleTracks.size, key = { "sub_$it" }) { index ->
                val track = subtitleTracks[index]
                TrackSelectorRow(
                    label = track.label,
                    isSelected = track.isSelected,
                    onClick = { onSubtitleTrackSelected(track.groupIndex, track.trackIndex) }
                )
            }
            item { 
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "⚙️ Altyazı Boyutu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Yazı Tipi Boyutu: ${subtitleSize.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TvSizeButton(
                            text = "-",
                            onClick = onSubtitleSizeDecrease
                        )
                        TvSizeButton(
                            text = "+",
                            onClick = onSubtitleSizeIncrease
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (isTv) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
            
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }),
                exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(360.dp)
                        .focusProperties {
                            exit = { FocusRequester.Cancel }
                        }
                        .focusGroup(),
                    color = Color(0xEE141414), // Şeffaf koyu gri arka plan
                    tonalElevation = 0.dp
                ) {
                    sheetContent()
                }
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = Color(0xFF141414)
        ) {
            sheetContent()
        }
    }
}

@Composable
private fun TrackSelectorRow(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val textColor by animateColorAsState(
        targetValue = if (isFocused) Color.Black else if (isSelected) Color(0xFFE50914) else Color.LightGray,
        animationSpec = tween(durationMillis = 150),
        label = "track_text_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "track_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .focusable(),
        color = if (isFocused) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isFocused) Color.Black else Color(0xFFE50914),
                    unselectedColor = if (isFocused) Color.DarkGray else Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun TvSizeButton(
    text: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val contentColor by animateColorAsState(
        targetValue = if (isFocused) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 150),
        label = "size_btn_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "size_btn_scale"
    )

    Surface(
        modifier = Modifier
            .size(42.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .focusable(),
        color = if (isFocused) Color.White else Color.DarkGray,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
