package com.yunusemre.m3ustream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yunusemre.m3ustream.R

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }

    // Üzerine gelindiğinde (odaklandığında) simge ve yazı rengi yumuşakça kırmızıya döner
    val activeColor by animateColorAsState(
        targetValue = if (isSearchFocused) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "search_focus_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Çizgisiz, üzerine gelindiğinde simge ve yazısı kırmızıya dönen arama kutusu
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isSearchFocused = it.isFocused }
                .focusable(),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = activeColor
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = activeColor
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                            tint = activeColor
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color(0xFFE50914),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = Color(0xFFE50914),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Bağımsız TV Mikrofon Butonu: Üzerine gelindiğinde kırmızıya döner
        TvIconButton(
            onClick = onVoiceSearchClick,
            icon = Icons.Filled.Mic,
            contentDescription = stringResource(R.string.voice_search),
            normalTint = Color.White.copy(alpha = 0.85f),
            focusedTint = Color(0xFFE50914),
            iconSize = 28.dp,
            containerSize = 52.dp,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )
    }
}
