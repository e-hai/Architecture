package xxx.yyy.zzz.feature.discover.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.analytics.Analytics
import com.kit.log.LogKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 发现探索页 ViewModel。
 *
 * @param repository 探索仓储
 */
class DiscoverViewModel(
    private val repository: DiscoverRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadBanners()
        loadTrendingTopics()
        loadVideos()
    }

    private fun loadBanners() {
        viewModelScope.launch {
            repository.getBanners().collect { banners ->
                _uiState.update { it.copy(banners = banners) }
            }
        }
    }

    private fun loadTrendingTopics() {
        viewModelScope.launch {
            repository.getTrendingTopics().collect { topics ->
                _uiState.update { it.copy(trendingTopics = topics) }
            }
        }
    }

    private fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentTopic = _uiState.value.selectedTopic
            val query = _uiState.value.searchQuery
            repository.getExploreVideos(currentTopic, query).collect { list ->
                _uiState.update { it.copy(videos = list, isLoading = false) }
            }
        }
    }

    /**
     * 搜索关键词输入更新。
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadVideos()
    }

    /**
     * 快捷分类/话题标签选择。
     */
    fun onTopicSelect(topic: String) {
        val nextTopic = if (topic == "全部") null else topic
        _uiState.update { it.copy(selectedTopic = nextTopic) }
        Analytics.logEvent("discover_category_click") {
            param("topic", topic)
        }
        LogKit.d("DiscoverViewModel", "Selected category: $topic")
        loadVideos()
    }

    /**
     * 点击热搜榜单项快速搜索/过滤。
     */
    fun onTrendingTopicClick(trending: TrendingTopic) {
        _uiState.update {
            it.copy(
                searchQuery = trending.title,
                selectedTopic = null,
            )
        }
        Analytics.logEvent("discover_trending_click") {
            param("rank", trending.rank.toString())
            param("title", trending.title)
        }
        LogKit.d("DiscoverViewModel", "Clicked trending topic: ${trending.title}")
        loadVideos()
    }
}
