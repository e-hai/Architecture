package xxx.yyy.zzz.feature.feed.impl

import androidx.compose.runtime.Immutable
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 短视频流 UI 状态。
 *
 * @property videos 视频列表
 * @property currentIndex 当前播放的视频索引
 * @property isLoading 是否正在刷新/加载
 * @property selectedTab 当前选中的顶栏 Tab (0: 关注, 1: 推荐)
 * @property errorMsg 错误提示信息
 */
@Immutable
data class FeedUiState(
    val videos: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val selectedTab: Int = 1,
    val errorMsg: String? = null,
)
