package com.yunusemre.m3ustream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.domain.model.Episode
import com.yunusemre.m3ustream.domain.model.WatchProgress

@Composable
fun EpisodeListItem(
    episode: Episode,
    progress: WatchProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.5.dp)
            .tvFocusable(
                onClick = onClick,
                focusedBorderWidth = 1.5.dp,
                focusedScale = 1.015f,
                cornerRadius = 8.dp
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bölüm Numarası Rozeti
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${episode.episodeNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bölüm Başlığı & İlerleme
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (progress != null && progress.progressPercent > 0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ProgressIndicator(
                        progress = progress.progressPercent,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Durum / Oynat Simgesi
            if (progress?.isCompleted == true) {
                Text(
                    text = stringResource(R.string.watched),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color(0xFFE50914).copy(alpha = 0.8f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.play),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
