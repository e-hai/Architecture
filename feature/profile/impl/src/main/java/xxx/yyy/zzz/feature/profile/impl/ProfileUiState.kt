package xxx.yyy.zzz.feature.profile.impl

import androidx.compose.runtime.Immutable
import xxx.yyy.zzz.core.model.Author
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 个人中心/用户主页 UI 状态。
 *
 * @property user 用户资料
 * @property followingCount 关注数
 * @property followerCount 粉丝数
 * @property totalLikedCount 获赞总数
 * @property selectedTab 当前选中的作品 Tab (0: 作品, 1: 喜欢, 2: 收藏)
 * @property works 发布的视频列表
 * @property likedVideos 点赞的视频列表
 * @property bookmarkedVideos 收藏的视频列表
 * @property isLoading 是否正在加载
 * @property errorMsg 错误提示
 */
@Immutable
data class ProfileUiState(
    val user: Author? = null,
    val followingCount: Long = 0L,
    val followerCount: Long = 0L,
    val totalLikedCount: Long = 0L,
    val selectedTab: Int = 0,
    val works: List<VideoItem> = emptyList(),
    val likedVideos: List<VideoItem> = emptyList(),
    val bookmarkedVideos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
)
