package com.yunusemre.m3ustream.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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

@OptIn(ExperimentalMaterial3Api::class)
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
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (audioTracks.isEmpty()) {
                item {
                    Text(
                        text = "Ses izi bulunamadı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                items(audioTracks) { track ->
                    TrackSelectorRow(
                        label = track.label,
                        isSelected = track.isSelected,
                        onClick = { onAudioTrackSelected(track.groupIndex, track.trackIndex) }
                    )
                }
                item { 
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Subtitles Section
            item {
                Text(
                    text = "📝 Altyazı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                val isOffSelected = subtitleTracks.none { it.isSelected }
                TrackSelectorRow(
                    label = "Kapalı",
                    isSelected = isOffSelected,
                    onClick = onSubtitleDisabled
                )
            }
            items(subtitleTracks) { track ->
                TrackSelectorRow(
                    label = track.label,
                    isSelected = track.isSelected,
                    onClick = { onSubtitleTrackSelected(track.groupIndex, track.trackIndex) }
                )
            }
            item { 
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "⚙️ Altyazı Boyutu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurface
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
}

@Composable
private fun TrackSelectorRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val textColor by animateColorAsState(
        targetValue = if (isFocused || isSelected) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "track_text_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.025f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "track_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .focusable(),
        color = if (isFocused) Color(0xFFE50914).copy(alpha = 0.14f) else if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFE50914),
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal,
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
        targetValue = if (isFocused) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "size_btn_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
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
        color = if (isFocused) Color(0xFFE50914).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
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
