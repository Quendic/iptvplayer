package com.yunusemre.m3ustream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.domain.model.Content

@Composable
fun ContentRow(
    title: String,
    contents: List<Content>,
    onContentClick: (Content) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (contents.isEmpty()) return

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (onSeeAllClick != null) {
                Text(
                    text = stringResource(R.string.see_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onSeeAllClick)
                )
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contents) { content ->
                ContentCard(
                    title = content.title,
                    posterUrl = content.posterUrl,
                    progress = content.progressPercent,
                    onClick = { onContentClick(content) }
                )
            }
        }
    }
}
