package com.yunusemre.m3ustream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android TV kumandası ile gezinirken öğelerin kibar, ince ve smooth şekilde
 * vurgulanmasını sağlayan modifier. drawWithContent ile çizildiği için resimler
 * veya çocuk bileşenler çerçeveyi asla kapatamaz.
 */
@Composable
fun Modifier.tvFocusable(
    onClick: (() -> Unit)? = null,
    focusedBorderColor: Color = Color(0xFFE50914), // Canlı Kırmızı
    focusedBorderWidth: Dp = 2.dp,                 // Kibar ve belirgin ince çizgi
    focusedScale: Float = 1.05f,                   // Smooth kibar büyüme
    cornerRadius: Dp = 10.dp,
    shape: Shape? = null,
    focusRequester: FocusRequester? = null,
    onFocusChange: ((Boolean) -> Unit)? = null
): Modifier {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "tv_focus_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) focusedBorderWidth else 0.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "tv_focus_border_width"
    )

    var mod = this
    if (focusRequester != null) {
        mod = mod.focusRequester(focusRequester)
    }

    return mod
        .onFocusChanged {
            isFocused = it.isFocused
            onFocusChange?.invoke(it.isFocused)
        }
        .scale(scale)
        .drawWithContent {
            // 1. Önce tüm içeriği (resim, metin vb.) çiz
            drawContent()
            // 2. Odaklandığında en üst katmana kırmızı ince çerçeveyi çiz (asla resmin altında kaybolmaz)
            if (isFocused && borderWidth > 0.dp) {
                val strokePx = borderWidth.toPx()
                val halfStroke = strokePx / 2f
                val radiusPx = cornerRadius.toPx()
                drawRoundRect(
                    color = focusedBorderColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = strokePx)
                )
            }
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
        .focusable()
}

/**
 * Üzerinde halka/yuvarlak olmadan, sadece simge rengini odaklandığında kırmızıya çeviren
 * ve hafifçe büyüten kibar TV ikon butonu.
 */
@Composable
fun TvIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    normalTint: Color = Color.White.copy(alpha = 0.85f),
    focusedTint: Color = Color(0xFFE50914), // Odaklandığında canlı kırmızı
    iconSize: Dp = 26.dp,
    containerSize: Dp = 44.dp
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_icon_scale"
    )
    val tintColor by animateColorAsState(
        targetValue = if (isFocused) focusedTint else normalTint,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tv_icon_color"
    )

    Box(
        modifier = modifier
            .size(containerSize)
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
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
