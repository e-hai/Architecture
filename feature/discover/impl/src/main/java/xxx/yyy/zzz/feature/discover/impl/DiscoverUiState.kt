package xxx.yyy.zzz.feature.discover.impl

import androidx.compose.runtime.Immutable
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 运营推荐/话题活动 Banner 数据项。
 */
@Immutable
data class DiscoverBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val coverUrl: String,
)

/**
 * 实时热搜/热点榜单数据项。
 */
@Immutable
data class TrendingTopic(
    val rank: Int,
    val title: String,
    val tag: String,
    val hotScore: String,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
)

/**
 * 发现页 UI 状态。
 *
 * @property searchQuery 搜索框当前输入的文字
 * @property selectedTopic 当前选中的话题/分类筛选（为 null 表示全量探索）
 * @property banners 顶部推荐活动 Banner 列表
 * @property trendingTopics 实时热搜榜单 Top 5
 * @property topics 快捷分类标签列表
 * @property videos 瀑布流推荐视频列表
 * @property isLoading 是否处于加载状态
 * @property errorMsg 错误提示信息
 */
@Immutable
data class DiscoverUiState(
    val searchQuery: String = "",
    val selectedTopic: String? = null,
    val banners: List<DiscoverBanner> = emptyList(),
    val trendingTopics: List<TrendingTopic> = emptyList(),
    val topics: List<String> = listOf("全部", "摄影日常", "治愈系风景", "极简生活", "手冲咖啡", "Kotlin/Compose"),
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
)
