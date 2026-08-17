package com.shortvideo.app.core.video

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.an.video.exoplayer.ExoHelper
import com.kit.log.LogKit
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.graphics.Color as AndroidColor

/**
 * 现代短视频全屏播放器 Composable 组件。
 *
 * 内置生命周期感知、无缝循环播放、本地 LRU 缓存加速、首帧渲染平滑渐变与封面无缝遮罩。
 *
 * @param videoUrl 视频流 URL（支持 HTTP/HTTPS 远程直链及本地缓存）
 * @param coverUrl 封面底图 URL
 * @param isActive 当前卡片在 ViewPager 中是否处于活跃/可见状态
 * @param isPlaying 是否处于播放状态（由外层单击暂停/播放驱动）
 * @param isMuted 是否静音播放
 * @param resizeMode 视频缩放模式（默认 RESIZE_MODE_ZOOM 填满屏幕）
 * @param onProgress 播放进度回调（当前毫秒数，总时长毫秒数）
 * @param onBuffering 缓冲状态变更回调
 */
@OptIn(UnstableApi::class)
@Composable
fun ShortVideoPlayer(
    videoUrl: String,
    coverUrl: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
    onProgress: (currentMs: Long, totalDurationMs: Long) -> Unit = { _, _ -> },
    onBuffering: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isFirstFrameRendered by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    // 初始化针对短视频微优化的 ExoPlayer 实例
    val exoPlayer =
        remember(videoUrl) {
            val loadControl =
                DefaultLoadControl
                    .Builder()
                    .setBufferDurationsMs(
                        1000, // 最小缓冲 1 秒即可起播
                        5000, // 最大缓冲 5 秒
                        500, // 播放前仅需 500ms 缓冲
                        1000, // 重启播放前仅需 1s 缓冲
                    ).build()

            ExoPlayer
                .Builder(context)
                .setLoadControl(loadControl)
                .build()
                .apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = if (isMuted) 0f else 1f
                    playWhenReady = isActive && isPlaying

                    val mediaSource = ExoHelper.createMediaSource(context, Uri.parse(videoUrl))
                    setMediaSource(mediaSource)
                    prepare()
                }
        }

    // 监听播放器状态事件
    DisposableEffect(exoPlayer) {
        val listener =
            object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    isFirstFrameRendered = true
                    isBuffering = false
                    onBuffering(false)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            isBuffering = true
                            onBuffering(true)
                        }

                        Player.STATE_READY -> {
                            isBuffering = false
                            onBuffering(false)
                        }

                        Player.STATE_ENDED -> {
                            isBuffering = false
                            onBuffering(false)
                        }

                        Player.STATE_IDLE -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    isError = true
                    isBuffering = false
                    onBuffering(false)
                    LogKit.e("ShortVideoPlayer", error, "Player error: ${error.message}")
                }
            }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // 活跃状态与暂停/播放动态响应
    LaunchedEffect(isActive, isPlaying) {
        exoPlayer.playWhenReady = isActive && isPlaying
    }

    // 静音状态动态响应
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // 实时上报播放进度
    LaunchedEffect(isActive, isPlaying) {
        while (this.isActive && isActive && isPlaying) {
            val duration = exoPlayer.duration.coerceAtLeast(0L)
            val currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (duration > 0) {
                onProgress(currentPosition, duration)
            }
            delay(100)
        }
    }

    // 绑定宿主生命周期，切后台自动暂停，切前台恢复
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        if (isActive && isPlaying) {
                            exoPlayer.playWhenReady = true
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        exoPlayer.playWhenReady = false
                    }

                    else -> {}
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        // 1. 底层真正的 ExoPlayer 视频画面渲染视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShutterBackgroundColor(AndroidColor.TRANSPARENT)
                    this.resizeMode = resizeMode
                    layoutParams =
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize(),
        )

        // 2. 封面图片层（首帧渲染完成前展示，完成后平滑淡出，彻底消除黑屏）
        AnimatedVisibility(
            visible = !isFirstFrameRendered,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // 3. 缓冲加载指示器
        if (isBuffering && isActive) {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .size(36.dp)
                        .align(Alignment.Center),
                color = Color.White.copy(alpha = 0.8f),
                strokeWidth = 2.5.dp,
            )
        }
    }
}
