package com.yunusemre.m3ustream.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yunusemre.m3ustream.ui.components.TvIconButton
import com.yunusemre.m3ustream.ui.components.tvFocusable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Center Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    TextButton(
                        onClick = onRewind,
                        modifier = Modifier.tvFocusable(
                            onClick = onRewind,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            focusedScale = 1.15f
                        )
                    ) {
                        Text(text = "-10s", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .tvFocusable(
                                onClick = onPlayPause,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                focusedScale = 1.18f
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    TextButton(
                        onClick = onForward,
                        modifier = Modifier.tvFocusable(
                            onClick = onForward,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            focusedScale = 1.15f
                        )
                    ) {
                        Text(text = "+10s", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { onSeek(it.toLong()) },
                            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TvIconButton(
                                onClick = onLockClick,
                                icon = Icons.Default.LockOpen,
                                contentDescription = "Lock"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = onSubtitleClick,
                                modifier = Modifier.tvFocusable(
                                    onClick = onSubtitleClick,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    focusedScale = 1.1f
                                )
                            ) {
                                Text("ALTYAZI", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = onAudioClick,
                                modifier = Modifier.tvFocusable(
                                    onClick = onAudioClick,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    focusedScale = 1.1f
                                )
                            ) {
                                Text("SES", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = onSpeedClick,
                                modifier = Modifier.tvFocusable(
                                    onClick = onSpeedClick,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    focusedScale = 1.1f
                                )
                            ) {
                                Text("${playbackSpeed}x", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (onNextEpisodeClick != null) {
                            Button(
                                onClick = onNextEpisodeClick,
                                modifier = Modifier.tvFocusable(
                                    onClick = onNextEpisodeClick,
                                    shape = ButtonDefaults.shape,
                                    focusedScale = 1.08f
                                ),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Sonraki Bölüm", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
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
