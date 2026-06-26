package xxx.yyy.zzz.feature.home.impl

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeResultViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeResultUiState())
    val uiState: StateFlow<HomeResultUiState> = _uiState.asStateFlow()

    fun initialize(title: String) {
        _uiState.value =
            HomeResultUiState(
                originalTitle = title,
                editedTitle = title,
            )
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(editedTitle = newTitle) }
    }

    data class HomeResultUiState(
        val originalTitle: String = "",
        val editedTitle: String = "",
    )
}
