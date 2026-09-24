package com.yunusemre.m3ustream.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.common.PlaybackException
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

@Composable
fun PlayerScreen(
    contentId: String,
    onNavigateUp: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val uiState by viewModel.uiState.collectAsState()

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(contentId) {
        viewModel.loadContent(contentId)
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // Daha genel bir tarayıcı user-agent'ı

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val player = ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        exoPlayer = player
        
        viewModel.applyTrackPreferences(player)

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.updatePlaybackState(isPlaying, player.currentPosition, player.duration)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                viewModel.setLoading(isBuffering)
                viewModel.updatePlaybackState(player.isPlaying, player.currentPosition, player.duration)
                if (playbackState == Player.STATE_READY) {
                    viewModel.loadTracks(player)
                    viewModel.setError(null)
                }
            }
            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                viewModel.loadTracks(player)
            }
            override fun onPlayerError(error: PlaybackException) {
                val cause = error.cause?.message ?: ""
                val code = error.errorCodeName
                viewModel.setError("Oynatma hatası: ${error.localizedMessage}\nKod: $code\nDetay: $cause")
            }
        })

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                player.pause()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveProgress(player.currentPosition, player.duration)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            viewModel.saveProgress(player.currentPosition, player.duration)
            player.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
            val uiModeManager = context.getSystemService(android.content.Context.UI_MODE_SERVICE) as? android.app.UiModeManager
            val isTv = uiModeManager?.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
            activity?.requestedOrientation = if (isTv) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    // Video oynadığı sürece ekranın uykuya geçmesini önleme
    DisposableEffect(uiState.isPlaying) {
        if (uiState.isPlaying) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Video oynarken kontrolleri otomatik gizleme (8 saniye sonra)
    LaunchedEffect(uiState.isControlsVisible, uiState.isPlaying, uiState.showTrackSelector) {
        if (uiState.isControlsVisible && uiState.isPlaying && !uiState.showTrackSelector) {
            delay(8000)
            viewModel.setControlsVisible(false)
        }
    }

    LaunchedEffect(uiState.content?.streamUrl, exoPlayer) {
        val url = uiState.content?.streamUrl
        val player = exoPlayer
        if (url != null && player != null) {
            val currentUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
            if (currentUri != url) {
                val mediaItem = MediaItem.fromUri(url)
                player.setMediaItem(mediaItem)
                if (uiState.currentPosition > 0) {
                    player.seekTo(uiState.currentPosition)
                }
                player.prepare()
                player.play()
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(5000)
            if (exoPlayer?.isPlaying == true) {
                viewModel.saveProgress(exoPlayer!!.currentPosition, exoPlayer!!.duration)
                viewModel.updatePlaybackState(true, exoPlayer!!.currentPosition, exoPlayer!!.duration)
            }
        }
    }
    
    LaunchedEffect(uiState.playbackSpeed) {
        exoPlayer?.setPlaybackSpeed(uiState.playbackSpeed)
    }

    BackHandler {
        if (uiState.showTrackSelector) {
            viewModel.setShowTrackSelector(false)
        } else if (uiState.isControlsVisible) {
            viewModel.setControlsVisible(false)
        } else {
            onNavigateUp()
        }
    }

    if (uiState.showTrackSelector && exoPlayer != null) {
        TrackSelectorSheet(
            audioTracks = uiState.audioTracks,
            subtitleTracks = uiState.subtitleTracks,
            onAudioTrackSelected = { group, track ->
                viewModel.selectAudioTrack(exoPlayer!!, group, track)
            },
            onSubtitleTrackSelected = { group, track ->
                viewModel.selectSubtitleTrack(exoPlayer!!, group, track)
            },
            onSubtitleDisabled = {
                viewModel.disableSubtitles(exoPlayer!!)
            },
            subtitleSize = uiState.subtitleSize,
            onSubtitleSizeIncrease = { viewModel.increaseSubtitleSize() },
            onSubtitleSizeDecrease = { viewModel.decreaseSubtitleSize() },
            onDismissRequest = { viewModel.setShowTrackSelector(false) }
        )
    }

    val rootFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    LaunchedEffect(uiState.isControlsVisible) {
        if (!uiState.isControlsVisible) {
            delay(50)
            try {
                rootFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val code = keyEvent.nativeKeyEvent.keyCode

                    // 1. Altyazı / Ses seçim paneli açıkken
                    if (uiState.showTrackSelector) {
                        if (code == KeyEvent.KEYCODE_BACK) {
                            viewModel.setShowTrackSelector(false)
                            return@onKeyEvent true
                        }
                        // Diğer yön tuşlarının panel içinde gezinmesine izin ver
                        return@onKeyEvent false
                    }

                    // 2. Kontroller GÖRÜNÜR durumdayken
                    if (uiState.isControlsVisible) {
                        when (code) {
                            KeyEvent.KEYCODE_BACK -> {
                                viewModel.setControlsVisible(false)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_MENU -> {
                                viewModel.setShowTrackSelector(!uiState.showTrackSelector)
                                return@onKeyEvent true
                            }
                            // D-Pad tuşlarını yutma! Compose odak sistemine bırakarak
                            // Play/Pause, Rewind, Forward, ALTYAZI, SES, Hız butonları arasında serbestçe gezinilmesini sağla
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                return@onKeyEvent false
                            }
                        }
                    } else {
                        // 3. Kontroller GİZLİ durumdayken (Tam ekran video oynarken)
                        when (code) {
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER,
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                if (exoPlayer?.isPlaying == true) {
                                    exoPlayer?.pause()
                                } else {
                                    exoPlayer?.play()
                                }
                                viewModel.setControlsVisible(true)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                exoPlayer?.play()
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                exoPlayer?.pause()
                                viewModel.setControlsVisible(true)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                                exoPlayer?.let {
                                    val pos = (it.currentPosition - 10000).coerceAtLeast(0)
                                    it.seekTo(pos)
                                    viewModel.updatePlaybackState(it.isPlaying, pos, it.duration)
                                }
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                                exoPlayer?.let {
                                    val pos = (it.currentPosition + 10000).coerceAtMost(it.duration)
                                    it.seekTo(pos)
                                    viewModel.updatePlaybackState(it.isPlaying, pos, it.duration)
                                }
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                viewModel.setControlsVisible(true)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_MENU -> {
                                viewModel.setShowTrackSelector(!uiState.showTrackSelector)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                onNavigateUp()
                                return@onKeyEvent true
                            }
                        }
                    }
                }
                false
            }
    ) {
        GestureHandler(
            window = activity!!.window,
            onSingleTap = { viewModel.toggleControls() },
            onDoubleTapLeft = {
                exoPlayer?.let {
                    val pos = (it.currentPosition - 10000).coerceAtLeast(0)
                    it.seekTo(pos)
                    viewModel.updatePlaybackState(it.isPlaying, pos, it.duration)
                }
            },
            onDoubleTapRight = {
                exoPlayer?.let {
                    val pos = (it.currentPosition + 10000).coerceAtMost(it.duration)
                    it.seekTo(pos)
                    viewModel.updatePlaybackState(it.isPlaying, pos, it.duration)
                }
            }
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        keepScreenOn = true
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        subtitleView?.let { sv ->
                            val style = androidx.media3.ui.CaptionStyleCompat(
                                android.graphics.Color.WHITE,
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT,
                                androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                                android.graphics.Color.BLACK,
                                android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                            )
                            sv.setStyle(style)
                            sv.setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 22f)
                            sv.setBottomPaddingFraction(0.08f)
                        }
                    }
                },
                update = { view ->
                    if (view.player != exoPlayer) {
                        view.player = exoPlayer
                    }
                    view.subtitleView?.setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, uiState.subtitleSize)
                }
            )
        }

        PlayerControls(
            isVisible = uiState.isControlsVisible,
            isLocked = uiState.isLocked,
            title = uiState.content?.title ?: "",
            isPlaying = uiState.isPlaying,
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            playbackSpeed = uiState.playbackSpeed,
            onBackClick = onNavigateUp,
            onPlayPause = {
                if (exoPlayer?.isPlaying == true) exoPlayer?.pause() else exoPlayer?.play()
            },
            onSeek = { pos ->
                exoPlayer?.seekTo(pos)
                viewModel.updatePlaybackState(exoPlayer?.isPlaying == true, pos, exoPlayer?.duration ?: 0)
            },
            onRewind = {
                exoPlayer?.let {
                    val pos = (it.currentPosition - 10000).coerceAtLeast(0)
                    it.seekTo(pos)
                    viewModel.updatePlaybackState(it.isPlaying, pos, it.duration)
                }
            },
            onForward = {
                exoPlayer?.let {
                    val pos = (it.currentPosition + 10000).coerceAtMost(it.duration)
                    it.seekTo(pos)
                    viewModel.updatePlaybackState(it.isPlaying, pos, it.duration)
                }
            },
            onSubtitleClick = { viewModel.setShowTrackSelector(true) },
            onAudioClick = { viewModel.setShowTrackSelector(true) },
            onSpeedClick = {
                val newSpeed = when (uiState.playbackSpeed) {
                    1.0f -> 1.25f
                    1.25f -> 1.5f
                    1.5f -> 2.0f
                    else -> 1.0f
                }
                viewModel.setPlaybackSpeed(newSpeed)
            },
            onLockClick = { viewModel.toggleLock() },
            onNextEpisodeClick = if (uiState.nextEpisode != null) {
                { viewModel.loadContent(uiState.nextEpisode!!.id) }
            } else null
        )

        if (uiState.isLoading && !uiState.isPlaying) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.error ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.setError(null)
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                }) {
                    Text("Tekrar Dene")
                }
            }
        }
    }
}
