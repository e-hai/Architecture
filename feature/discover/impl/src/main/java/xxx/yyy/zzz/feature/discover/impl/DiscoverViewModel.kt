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
 * 发现页 ViewModel。
 *
 * @param repository 探索仓储
 */
class DiscoverViewModel(
    private val repository: DiscoverRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadTopics()
        loadVideos()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            repository.getTrendingTopics().collect { topics ->
                _uiState.update { it.copy(topics = topics) }
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
     * 搜索关键词输入。
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadVideos()
    }

    /**
     * 话题标签选择。
     */
    fun onTopicSelect(topic: String) {
        val nextTopic = if (topic == "全部") null else topic
        _uiState.update { it.copy(selectedTopic = nextTopic) }
        Analytics.logEvent("discover_topic_click") {
            param("topic", topic)
        }
        LogKit.d("DiscoverViewModel", "Selected topic: $topic")
        loadVideos()
    }
}
