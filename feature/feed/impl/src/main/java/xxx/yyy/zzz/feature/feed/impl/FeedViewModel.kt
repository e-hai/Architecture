package xxx.yyy.zzz.feature.feed.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.analytics.Analytics
import com.kit.log.LogKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.analytics.AnalyticsParams

/**
 * 首页短视频流 ViewModel。
 *
 * @param repository 短视频数据仓储
 */
class FeedViewModel(
    private val repository: FeedRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        observeVideos()
    }

    private fun observeVideos() {
        viewModelScope.launch {
            repository.getFeedVideos().collect { list ->
                if (list.isEmpty()) {
                    // 本地尚无缓存时，触发一次预置种子数据同步
                    repository.refreshFeed()
                } else {
                    _uiState.update { it.copy(videos = list, isLoading = false) }
                }
            }
        }
    }

    /**
     * 视频滑动翻页事件。
     */
    fun onPageChanged(pageIndex: Int) {
        _uiState.update { it.copy(currentIndex = pageIndex) }
        val currentVideo = _uiState.value.videos.getOrNull(pageIndex)
        if (currentVideo != null) {
            Analytics.logEvent(AnalyticsEvents.Common.IMPRESSION) {
                param(AnalyticsParams.ITEM_ID, currentVideo.id)
                param("title", currentVideo.title)
            }
            LogKit.d("FeedViewModel", "Page changed to index=$pageIndex, videoId=${currentVideo.id}")
        }
    }

    /**
     * 切换顶部 Tab (0: 关注, 1: 推荐)。
     */
    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    /**
     * 视频点赞/取消点赞。
     */
    fun onToggleLike(videoId: String) {
        viewModelScope.launch {
            repository.toggleLike(videoId)
            Analytics.logEvent("video_like") {
                param("video_id", videoId)
            }
        }
    }

    /**
     * 视频收藏/取消收藏。
     */
    fun onToggleBookmark(videoId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(videoId)
            Analytics.logEvent("video_bookmark") {
                param("video_id", videoId)
            }
        }
    }
}
