package com.shortvideo.app.feature.feed.impl

import androidx.compose.runtime.Immutable
import com.shortvideo.app.core.model.VideoItem

/**
 * 推荐短视频流 UI 状态。
 *
 * @property videos 推荐视频列表
 * @property currentIndex 当前播放的视频索引
 * @property isLoading 是否正在加载中
 * @property errorMsg 错误提示信息
 */
@Immutable
data class FeedUiState(
    val videos: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
)
