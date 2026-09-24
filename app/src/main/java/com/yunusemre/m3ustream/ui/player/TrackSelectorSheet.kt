package com.yunusemre.m3ustream.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunusemre.m3ustream.ui.components.tvFocusable

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tvFocusable(
                                onClick = { onAudioTrackSelected(track.groupIndex, track.trackIndex) },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                focusedScale = 1.02f
                            )
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = track.isSelected,
                            onClick = { onAudioTrackSelected(track.groupIndex, track.trackIndex) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = track.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (track.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(
                            onClick = onSubtitleDisabled,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            focusedScale = 1.02f
                        )
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isOffSelected,
                        onClick = onSubtitleDisabled,
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Kapalı",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isOffSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isOffSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            items(subtitleTracks) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(
                            onClick = { onSubtitleTrackSelected(track.groupIndex, track.trackIndex) },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            focusedScale = 1.02f
                        )
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = track.isSelected,
                        onClick = { onSubtitleTrackSelected(track.groupIndex, track.trackIndex) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = track.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (track.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Yazı Tipi Boyutu: ${subtitleSize.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = onSubtitleSizeDecrease,
                            modifier = Modifier.tvFocusable(
                                onClick = onSubtitleSizeDecrease,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                focusedScale = 1.15f
                            )
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = onSubtitleSizeIncrease,
                            modifier = Modifier.tvFocusable(
                                onClick = onSubtitleSizeIncrease,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                focusedScale = 1.15f
                            )
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
