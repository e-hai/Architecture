package xxx.yyy.zzz.feature.discover.impl

import androidx.compose.runtime.Immutable
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 热门探索话题数据项。
 */
@Immutable
data class TopicItem(
    val id: String,
    val title: String,
    val heatCount: String,
    val iconUrl: String? = null,
)

/**
 * 发现页 UI 状态。
 *
 * @property searchQuery 搜索框当前输入的文字
 * @property selectedTopic 当前选中的话题筛选（为 null 表示全量推荐）
 * @property topics 热门话题列表
 * @property videos 瀑布流推荐视频列表
 * @property isLoading 是否处于刷新或加载状态
 * @property errorMsg 错误提示信息
 */
@Immutable
data class DiscoverUiState(
    val searchQuery: String = "",
    val selectedTopic: String? = null,
    val topics: List<TopicItem> = emptyList(),
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
)
