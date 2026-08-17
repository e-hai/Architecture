package com.shortvideo.app.feature.feed.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shortvideo.app.core.video.VideoPreloader
import org.koin.androidx.compose.koinViewModel

/**
 * 首页/二级视频播放流页面 Composable。
 *
 * 遵循画报美学与极简原则：全屏沉浸播放，支持定位初始视频及从发现页/个人页进入时的二级返回操作。
 *
 * @param initialVideoId 初始定位播放的视频 ID，为空时从头推荐
 * @param onBackClick 二级页面返回回调（为 null 时代表一级首页，不展示返回键）
 * @param onAuthorClick 点击作者头像/昵称回调
 * @param onCommentClick 点击评论按钮回调
 * @param viewModel 首页 ViewModel
 */
@Composable
fun FeedScreen(
    initialVideoId: String? = null,
    onBackClick: (() -> Unit)? = null,
    onAuthorClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {},
    viewModel: FeedViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val initialPageIndex =
        if (initialVideoId != null && uiState.videos.isNotEmpty()) {
            uiState.videos.indexOfFirst { it.id == initialVideoId }.coerceAtLeast(0)
        } else {
            uiState.currentIndex
        }

    val pagerState =
        rememberPagerState(
            initialPage = initialPageIndex,
            pageCount = { uiState.videos.size },
        )

    // 若数据异步加载完毕且传入了 initialVideoId，自动滚动至目标位置
    LaunchedEffect(initialVideoId, uiState.videos) {
        if (initialVideoId != null && uiState.videos.isNotEmpty()) {
            val targetIndex = uiState.videos.indexOfFirst { it.id == initialVideoId }
            if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
                pagerState.scrollToPage(targetIndex)
            }
        }
    }

    // 滑动翻页监听与数据打点
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onPageChanged(page)
        }
    }

    // 后台智能预加载相邻视频分片，保障下滑秒开无等待
    LaunchedEffect(pagerState.currentPage, uiState.videos) {
        val currentIndex = pagerState.currentPage
        val preloadUrls =
            listOfNotNull(
                uiState.videos.getOrNull(currentIndex + 1)?.videoUrl,
                uiState.videos.getOrNull(currentIndex + 2)?.videoUrl,
            )
        if (preloadUrls.isNotEmpty()) {
            VideoPreloader.preload(context, preloadUrls)
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF0E0D0C)),
    ) {
        if (uiState.isLoading && uiState.videos.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFE5C384),
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

        // 二级页面返回悬浮按钮（从发现页/个人主页进入时展示）
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .padding(start = 14.dp, top = 8.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.5f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFFFAF6EE),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
