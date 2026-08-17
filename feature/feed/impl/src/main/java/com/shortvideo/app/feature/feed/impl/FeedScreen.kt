package com.shortvideo.app.feature.feed.impl

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * 首页短视频流页面 Composable。
 *
 * @param onCameraClick 点击拍摄按钮回调
 * @param onAuthorClick 点击作者头像/昵称回调
 * @param onCommentClick 点击评论按钮回调
 * @param viewModel 首页 ViewModel
 */
@Composable
fun FeedScreen(
    onCameraClick: () -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {},
    viewModel: FeedViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState =
        rememberPagerState(
            initialPage = uiState.currentIndex,
            pageCount = { uiState.videos.size },
        )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onPageChanged(page)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        if (uiState.isLoading && uiState.videos.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        } else if (uiState.videos.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                val video = uiState.videos[page]
                FeedItemView(
                    video = video,
                    isActive = (page == pagerState.currentPage),
                    onLikeClick = { viewModel.onToggleLike(video.id) },
                    onCommentClick = { onCommentClick(video.id) },
                    onBookmarkClick = { viewModel.onToggleBookmark(video.id) },
                    onShareClick = { /* 触发系统分享或 Kit 分享 */ },
                    onAuthorClick = { onAuthorClick(video.author.id) },
                )
            }
        }

        // 精简顶栏：仅保留创作拍摄入口与关注/推荐 Tab，保证纯粹沉浸体验
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 左侧占位以保持中间居中
            Spacer(modifier = Modifier.size(36.dp))

            // 中间 Tab (关注 / 推荐)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TopTabItem(
                    title = "关注",
                    isSelected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                )
                TopTabItem(
                    title = "推荐",
                    isSelected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                )
            }

            // 右侧创作拍摄入口
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "拍摄",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun TopTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = if (isSelected) 18.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(
                modifier =
                    Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.White),
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
