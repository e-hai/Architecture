package com.shortvideo.app.feature.feed.impl

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.shortvideo.app.core.model.VideoItem

/**
 * 方案 D（画报杂志生活美学流）视频交互浮层。
 *
 * @param video 视频领域模型
 * @param onBackClick 二级页面返回回调（为 null 表示一级首页）
 * @param onLikeClick 点击点赞回调
 * @param onCommentClick 点击评论回调
 * @param onBookmarkClick 点击收藏回调
 * @param onShareClick 点击分享回调
 * @param onAuthorClick 点击作者头像回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedInteractionOverlay(
    video: VideoItem,
    onBackClick: (() -> Unit)? = null,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "disc_rotation",
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f),
                            ),
                    ),
                ).navigationBarsPadding(),
    ) {
        // 1. 顶部画报顶栏 (支持返回按钮与画报标题水平并排，避免任何遮挡)
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.5f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0xFFFAF6EE),
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier =
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5C384)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CURATED DAILY · 每日画报",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE5C384),
                letterSpacing = 1.5.sp,
            )
        }

        // 2. 右侧操作栏（纵向温润圆角胶囊布局）
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 作者头像 + 关注金标
            Box(contentAlignment = Alignment.BottomCenter) {
                AsyncImage(
                    model = video.author.avatarUrl,
                    contentDescription = video.author.nickname,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFE5C384).copy(alpha = 0.8f), CircleShape)
                            .clickable(onClick = onAuthorClick),
                )
                if (!video.author.isFollowing) {
                    Box(
                        modifier =
                            Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5C384)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "关注",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }

            // 点赞
            EditorialActionButton(
                icon = if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = formatCount(video.likeCount),
                tint = if (video.isLiked) Color(0xFFFF4066) else Color(0xFFFAF6EE),
                onClick = onLikeClick,
            )

            // 评论
            EditorialActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                text = formatCount(video.commentCount),
                tint = Color(0xFFFAF6EE),
                onClick = onCommentClick,
            )

            // 收藏
            EditorialActionButton(
                icon = if (video.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                text = formatCount(video.bookmarkCount),
                tint = if (video.isBookmarked) Color(0xFFE5C384) else Color(0xFFFAF6EE),
                onClick = onBookmarkClick,
            )

            // 分享
            EditorialActionButton(
                icon = Icons.Default.Share,
                text = formatCount(video.shareCount),
                tint = Color(0xFFFAF6EE),
                onClick = onShareClick,
            )

            // 画报光影旋转唱片
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141312))
                        .border(1.dp, Color(0xFFE5C384).copy(alpha = 0.4f), CircleShape)
                        .rotate(discRotation),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = video.music?.coverUrl.takeUnless { it.isNullOrBlank() } ?: video.author.avatarUrl,
                    contentDescription = "音乐封面",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                )
            }
        }

        // 3. 底部作者与画报标题排版
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.76f)
                    .padding(start = 18.dp, bottom = 32.dp),
        ) {
            // 作者 handle
            Text(
                text = video.author.nickname,
                fontSize = 15.sp,
                color = Color(0xFFFAF6EE),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                modifier = Modifier.clickable(onClick = onAuthorClick),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 视频标题（典雅排版）
            Text(
                text = video.title,
                fontSize = 13.sp,
                color = Color(0xFFFAF6EE).copy(alpha = 0.9f),
                lineHeight = 19.sp,
                maxLines = 3,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 美学标签 Pills
            if (video.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    video.tags.take(3).forEach { tag ->
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33E5C384))
                                    .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.5.dp),
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = Color(0xFFE5C384),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 音乐原声行
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "音乐",
                    tint = Color(0xFFE5C384),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = video.music?.title ?: "原声音乐 - ${video.author.nickname}",
                    fontSize = 11.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.75f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EditorialActionButton(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(30.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text,
            color = Color(0xFFFAF6EE).copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatCount(count: Long): String =
    when {
        count >= 10000 -> String.format("%.1fw", count / 10000.0)
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
