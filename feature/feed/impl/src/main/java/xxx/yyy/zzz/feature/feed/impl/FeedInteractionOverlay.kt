package xxx.yyy.zzz.feature.feed.impl

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 视频交互浮层（包含右侧操作栏、底部创作者信息与背景音乐旋转唱片）。
 *
 * @param video 视频领域模型
 * @param onLikeClick 点击点赞回调
 * @param onCommentClick 点击评论回调
 * @param onBookmarkClick 点击收藏回调
 * @param onShareClick 点击分享回调
 * @param onAuthorClick 点击作者头像回调
 */
@Composable
fun FeedInteractionOverlay(
    video: VideoItem,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 旋转唱片无限循环动画
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
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
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                            ),
                    ),
                ).navigationBarsPadding(),
    ) {
        // 右侧操作栏
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 作者头像 + 关注加号
            Box(contentAlignment = Alignment.BottomCenter) {
                AsyncImage(
                    model = video.author.avatarUrl,
                    contentDescription = video.author.nickname,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onAuthorClick)
                            .background(Color.White.copy(alpha = 0.2f)),
                )
                if (!video.author.isFollowing) {
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2C55)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "关注",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            // 点赞
            ActionButton(
                icon = if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = formatCount(video.likeCount),
                tint = if (video.isLiked) Color(0xFFFF2C55) else Color.White,
                onClick = onLikeClick,
            )

            // 评论
            ActionButton(
                icon = Icons.Default.ChatBubble,
                text = formatCount(video.commentCount),
                tint = Color.White,
                onClick = onCommentClick,
            )

            // 收藏
            ActionButton(
                icon = if (video.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                text = formatCount(video.bookmarkCount),
                tint = if (video.isBookmarked) Color(0xFFFFCC00) else Color.White,
                onClick = onBookmarkClick,
            )

            // 分享
            ActionButton(
                icon = Icons.Default.Share,
                text = formatCount(video.shareCount),
                tint = Color.White,
                onClick = onShareClick,
            )

            // 旋转唱片
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .rotate(discRotation),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = video.music?.coverUrl.takeUnless { it.isNullOrBlank() } ?: video.author.avatarUrl,
                    contentDescription = "音乐封面",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape),
                )
            }
        }

        // 底部作者信息与标题
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.78f)
                    .padding(start = 16.dp, bottom = 48.dp),
        ) {
            Text(
                text = video.author.nickname,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAuthorClick),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 20.sp,
                maxLines = 3,
            )
            Spacer(modifier = Modifier.height(10.dp))
            // 音乐原声行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "音乐",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = video.music?.title ?: "原声音乐 - ${video.author.nickname}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
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
            modifier = Modifier.size(34.dp),
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
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
