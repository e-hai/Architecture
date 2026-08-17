package com.shortvideo.app.feature.feed.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shortvideo.app.core.model.VideoItem
import com.shortvideo.app.core.video.ShortVideoPlayer
import kotlinx.coroutines.delay

/**
 * 单页短视频播放容器组件（包含真实视频播放流、双击飞心动效与交互浮层）。
 *
 * @param video 视频领域模型
 * @param isActive 是否为当前屏幕可见活跃页面
 * @param onLikeClick 点赞回调
 * @param onCommentClick 评论回调
 * @param onBookmarkClick 收藏回调
 * @param onShareClick 分享回调
 * @param onAuthorClick 作者主页回调
 */
@Composable
fun FeedItemView(
    video: VideoItem,
    isActive: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    val heartAnimList = remember { mutableStateListOf<HeartParticle>() }

    // 页面进入/离开当前可见区域时的播放控制
    LaunchedEffect(isActive) {
        isPlaying = isActive
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            onLikeClick()
                            heartAnimList.add(HeartParticle(id = System.currentTimeMillis(), offset = offset))
                        },
                        onTap = {
                            isPlaying = !isPlaying
                        },
                    )
                },
    ) {
        // 1. 核心真实短视频播放器组件
        ShortVideoPlayer(
            videoUrl = video.videoUrl,
            coverUrl = video.coverUrl,
            isActive = isActive,
            isPlaying = isPlaying,
            onProgress = { currentMs, totalMs ->
                if (totalMs > 0) {
                    playbackProgress = currentMs.toFloat() / totalMs.toFloat()
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // 2. 暂停状态居中大图标
        AnimatedVisibility(
            visible = !isPlaying && isActive,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(72.dp)
                        .background(Color.Black.copy(alpha = 0.45f), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "暂停中",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        // 3. 双击飞心动画列表
        heartAnimList.forEach { particle ->
            DoubleTapHeartAnimation(
                particle = particle,
                onFinished = { heartAnimList.remove(particle) },
            )
        }

        // 4. 右侧互动条与底部作者/文案浮层
        FeedInteractionOverlay(
            video = video,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onBookmarkClick = onBookmarkClick,
            onShareClick = onShareClick,
            onAuthorClick = onAuthorClick,
            modifier = Modifier.fillMaxSize(),
        )

        // 5. 底部真实播放进度条
        LinearProgressIndicator(
            progress = { playbackProgress },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp),
            color = Color.White.copy(alpha = 0.85f),
            trackColor = Color.Transparent,
        )
    }
}

/**
 * 双击点赞爱心粒子。
 */
data class HeartParticle(
    val id: Long,
    val offset: Offset,
)

@Composable
private fun DoubleTapHeartAnimation(
    particle: HeartParticle,
    onFinished: () -> Unit,
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(particle.id) {
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 150),
        )
        delay(200)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 250),
        )
        onFinished()
    }

    Box(
        modifier =
            Modifier
                .offset { IntOffset(particle.offset.x.toInt() - 60, particle.offset.y.toInt() - 60) }
                .scale(scale.value),
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "点赞动画",
            tint = Color(0xFFFF2C55).copy(alpha = alpha.value),
            modifier = Modifier.size(80.dp),
        )
    }
}
