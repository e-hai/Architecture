package xxx.yyy.zzz.feature.profile.impl

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
 * 个人中心 ViewModel。
 *
 * @param repository 个人中心仓储
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * 载入指定用户的资料与作品。
     */
    fun loadUserProfile(userId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getUserProfile(userId).collect { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        followingCount = 186L,
                        followerCount = user.followerCount,
                        totalLikedCount = 289000L,
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getUserWorks(userId).collect { works ->
                _uiState.update { it.copy(works = works) }
            }
        }

        viewModelScope.launch {
            repository.getLikedVideos().collect { likes ->
                _uiState.update { it.copy(likedVideos = likes) }
            }
        }

        viewModelScope.launch {
            repository.getBookmarkedVideos().collect { bookmarks ->
                _uiState.update { it.copy(bookmarkedVideos = bookmarks, isLoading = false) }
            }
        }
    }

    /**
     * 切换作品/喜欢/收藏 Tab (0: 作品, 1: 喜欢, 2: 收藏)。
     */
    fun onTabSelect(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        Analytics.logEvent("profile_tab_switch") {
            param("tab_index", tabIndex.toString())
        }
        LogKit.d("ProfileViewModel", "Tab switched to: $tabIndex")
    }
}
