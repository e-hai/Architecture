package com.shortvideo.app.feature.feed.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * 首页推荐短视频流页面 Composable。
 *
 * 遵循极简纯粹的设计原则：无冗余顶栏遮挡，全屏沉浸播放推荐短视频内容。
 *
 * @param onAuthorClick 点击作者头像/昵称回调
 * @param onCommentClick 点击评论按钮回调
 * @param viewModel 首页 ViewModel
 */
@Composable
fun FeedScreen(
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
    }
}
