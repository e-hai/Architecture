package xxx.yyy.zzz.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.data.UserRepository
import xxx.yyy.zzz.core.model.ListItem
import xxx.yyy.zzz.core.model.User

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val featuredItems: List<ListItem>,
        val recentItems: List<ListItem>
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _featuredItems = listOf(
        ListItem(id = "1", title = "Featured Item 1"),
        ListItem(id = "2", title = "Featured Item 2"),
        ListItem(id = "3", title = "Featured Item 3"),
        ListItem(id = "4", title = "Featured Item 4"),
        ListItem(id = "5", title = "Featured Item 5")
    )

    private val _recentItems = List(10) { index ->
        ListItem(id = "r${index + 1}", title = "Regular Item ${index + 1}")
    }

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

            _uiState.value = HomeUiState.Success(
                featuredItems = _featuredItems,
                recentItems = _recentItems
            )
        }
    }

    fun onSyncClick() {
        viewModelScope.launch {
            userRepository.syncUser("1")
        }
    }

    fun updateItemTitle(itemId: String, newTitle: String) {
        _uiState.update { currentState ->
            if (currentState is HomeUiState.Success) {
                val updatedFeaturedItems = currentState.featuredItems.map {
                    if (it.id == itemId) it.copy(title = newTitle) else it
                }
                val updatedRecentItems = currentState.recentItems.map {
                    if (it.id == itemId) it.copy(title = newTitle) else it
                }
                currentState.copy(
                    featuredItems = updatedFeaturedItems,
                    recentItems = updatedRecentItems
                )
            } else {
                currentState
            }
        }
    }
}
