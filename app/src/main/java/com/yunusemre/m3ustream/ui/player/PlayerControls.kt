package com.yunusemre.m3ustream.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunusemre.m3ustream.ui.components.TvIconButton
import java.util.Locale

@Composable
fun PlayerControls(
    isVisible: Boolean,
    isLocked: Boolean,
    title: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    onBackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onLockClick: () -> Unit,
    onNextEpisodeClick: (() -> Unit)? = null,
    playPauseFocusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        ) {
            if (isLocked) {
                IconButton(
                    onClick = onLockClick,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvIconButton(
                        onClick = onBackClick,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Center Controls (Rewind, Play/Pause, Forward)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(36.dp)
                ) {
                    TvTextButton(
                        text = "-10s",
                        onClick = onRewind
                    )

                    TvPlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = onPlayPause,
                        focusRequester = playPauseFocusRequester
                    )

                    TvTextButton(
                        text = "+10s",
                        onClick = onForward
                    )
                }

                // Bottom Controls (Slider & Action Buttons)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Timeline Slider (touch only, D-Pad skips to avoid trapping navigation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { onSeek(it.toLong()) },
                            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .focusProperties { canFocus = false },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFE50914),
                                activeTrackColor = Color(0xFFE50914),
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TvIconButton(
                                onClick = onLockClick,
                                icon = Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                containerSize = 40.dp,
                                iconSize = 22.dp
                            )

                            TvTextButton(
                                text = "ALTYAZI",
                                icon = Icons.Default.Subtitles,
                                onClick = onSubtitleClick
                            )

                            TvTextButton(
                                text = "SES",
                                icon = Icons.Default.Audiotrack,
                                onClick = onAudioClick
                            )

                            TvTextButton(
                                text = "${playbackSpeed}x",
                                icon = Icons.Default.Speed,
                                onClick = onSpeedClick
                            )
                        }

                        if (onNextEpisodeClick != null) {
                            TvNextEpisodeButton(
                                onClick = onNextEpisodeClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * TV kumandası ile odaklandığında yazısı ve simgesi kırmızıya dönen,
 * yumuşakça ölçeklenen çizgisiz TV buton bileşeni.
 */
@Composable
fun TvTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val textColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFFE50914) else Color.White,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_text_btn_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_text_btn_scale"
    )

    var mod = modifier
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .focusable()

    if (focusRequester != null) {
        mod = mod.focusRequester(focusRequester)
    }

    Surface(
        modifier = mod,
        color = if (isFocused) Color(0xFFE50914).copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Ortadaki büyük Oynat/Duraklat butonu.
 */
@Composable
fun TvPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val tintColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFFE50914) else Color.White,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_play_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.18f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_play_scale"
    )

    var mod = modifier
        .size(68.dp)
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .focusable()

    if (focusRequester != null) {
        mod = mod.focusRequester(focusRequester)
    }

    Box(
        modifier = mod,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = tintColor,
            modifier = Modifier.size(54.dp)
        )
    }
}

/**
 * Sonraki Bölüm butonu.
 */
@Composable
fun TvNextEpisodeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val textColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFFE50914) else Color.White,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_next_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_next_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .focusable(),
        color = if (isFocused) Color(0xFFE50914).copy(alpha = 0.18f) else Color(0xFFE50914),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Sonraki Bölüm",
            color = if (isFocused) Color(0xFFE50914) else Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
