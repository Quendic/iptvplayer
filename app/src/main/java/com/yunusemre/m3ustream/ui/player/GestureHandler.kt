package com.yunusemre.m3ustream.ui.player

import android.content.Context
import android.media.AudioManager
import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GestureHandler(
    window: Window,
    onSingleTap: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    var overlayText by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(overlayText) {
        if (overlayText != null) {
            delay(1500)
            overlayText = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 2) {
                            onDoubleTapLeft()
                        } else {
                            onDoubleTapRight()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var startY = 0f
                var startVol = 0
                var startBrightness = 0f
                var isRightHalf = false
                
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        startY = offset.y
                        isRightHalf = offset.x > size.width / 2
                        if (isRightHalf) {
                            startVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        } else {
                            startBrightness = window.attributes.screenBrightness
                            if (startBrightness < 0) startBrightness = 0.5f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val diff = (startY - change.position.y) / size.height
                        if (isRightHalf) {
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val newVol = (startVol + diff * maxVol).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            overlayText = "Ses: %${(newVol * 100 / maxVol)}"
                        } else {
                            val newBrightness = (startBrightness + diff).coerceIn(0f, 1f)
                            val lp = window.attributes
                            lp.screenBrightness = newBrightness
                            window.attributes = lp
                            overlayText = "Parlaklık: %${(newBrightness * 100).toInt()}"
                        }
                    }
                )
            }
    ) {
        content()
        
        if (overlayText != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = overlayText!!,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
