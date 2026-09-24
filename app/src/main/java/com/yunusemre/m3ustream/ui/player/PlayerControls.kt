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
import kotlinx.coroutines.delay
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
    modifier: Modifier = Modifier
) {
    // D-Pad gezinimi için net odak referansları
    val playPauseFocusRequester = remember { FocusRequester() }
    val rewindFocusRequester = remember { FocusRequester() }
    val forwardFocusRequester = remember { FocusRequester() }
    val backFocusRequester = remember { FocusRequester() }
    val lockFocusRequester = remember { FocusRequester() }
    val altyaziFocusRequester = remember { FocusRequester() }
    val sesFocusRequester = remember { FocusRequester() }
    val speedFocusRequester = remember { FocusRequester() }
    val nextEpisodeFocusRequester = remember { FocusRequester() }

    // Kontroller açıldığında odağı KESİNLİKLE Oynat/Duraklat butonuna ver (Asla Geri butonuna gitmesin!)
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(50)
            try { playPauseFocusRequester.requestFocus() } catch (_: Exception) {}
            delay(100)
            try { playPauseFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

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
                // Tek bir dikey sütun (Column): Odak sisteminin dikey hiyerarşiyi eksiksiz kavramasını sağlar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. ÜST BAR (Geri butonu ve Başlık)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvIconButton(
                            onClick = onBackClick,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .focusRequester(backFocusRequester)
                                .focusProperties {
                                    down = playPauseFocusRequester
                                    right = playPauseFocusRequester
                                }
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

                    // 2. ORTA KONTROLLER (Geri Sar, Oynat/Duraklat, İleri Sar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvTextButton(
                            text = "-10s",
                            onClick = onRewind,
                            modifier = Modifier
                                .focusRequester(rewindFocusRequester)
                                .focusProperties {
                                    up = backFocusRequester
                                    right = playPauseFocusRequester
                                    down = altyaziFocusRequester
                                }
                        )

                        Spacer(modifier = Modifier.width(36.dp))

                        TvPlayPauseButton(
                            isPlaying = isPlaying,
                            onClick = onPlayPause,
                            modifier = Modifier
                                .focusRequester(playPauseFocusRequester)
                                .focusProperties {
                                    up = backFocusRequester
                                    left = rewindFocusRequester
                                    right = forwardFocusRequester
                                    down = altyaziFocusRequester
                                }
                        )

                        Spacer(modifier = Modifier.width(36.dp))

                        TvTextButton(
                            text = "+10s",
                            onClick = onForward,
                            modifier = Modifier
                                .focusRequester(forwardFocusRequester)
                                .focusProperties {
                                    up = backFocusRequester
                                    left = playPauseFocusRequester
                                    down = sesFocusRequester
                                }
                        )
                    }

                    // 3. ALT KONTROLLER (Süre Çubuğu, ALTYAZI, SES, Hız, Sonraki Bölüm)
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Süre Çubuğu (Kumandanın odak tuzağına düşmemesi için focus almaz, dokunmatik çalışır)
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

                        // Alt Buton Satırı (ALTYAZI, SES vb.)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TvIconButton(
                                    onClick = onLockClick,
                                    icon = Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    containerSize = 40.dp,
                                    iconSize = 22.dp,
                                    modifier = Modifier
                                        .focusRequester(lockFocusRequester)
                                        .focusProperties {
                                            up = rewindFocusRequester
                                            right = altyaziFocusRequester
                                        }
                                )

                                TvTextButton(
                                    text = "ALTYAZI",
                                    icon = Icons.Default.Subtitles,
                                    onClick = onSubtitleClick,
                                    modifier = Modifier
                                        .focusRequester(altyaziFocusRequester)
                                        .focusProperties {
                                            up = playPauseFocusRequester
                                            left = lockFocusRequester
                                            right = sesFocusRequester
                                        }
                                )

                                TvTextButton(
                                    text = "SES",
                                    icon = Icons.Default.Audiotrack,
                                    onClick = onAudioClick,
                                    modifier = Modifier
                                        .focusRequester(sesFocusRequester)
                                        .focusProperties {
                                            up = playPauseFocusRequester
                                            left = altyaziFocusRequester
                                            right = speedFocusRequester
                                        }
                                )

                                TvTextButton(
                                    text = "${playbackSpeed}x",
                                    icon = Icons.Default.Speed,
                                    onClick = onSpeedClick,
                                    modifier = Modifier
                                        .focusRequester(speedFocusRequester)
                                        .focusProperties {
                                            up = forwardFocusRequester
                                            left = sesFocusRequester
                                            right = if (onNextEpisodeClick != null) nextEpisodeFocusRequester else FocusRequester.Default
                                        }
                                )
                            }

                            if (onNextEpisodeClick != null) {
                                TvNextEpisodeButton(
                                    onClick = onNextEpisodeClick,
                                    modifier = Modifier
                                        .focusRequester(nextEpisodeFocusRequester)
                                        .focusProperties {
                                            up = forwardFocusRequester
                                            left = speedFocusRequester
                                        }
                                )
                            }
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
    icon: ImageVector? = null
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
    modifier: Modifier = Modifier
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

    Box(
        modifier = modifier
            .size(68.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .focusable(),
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
