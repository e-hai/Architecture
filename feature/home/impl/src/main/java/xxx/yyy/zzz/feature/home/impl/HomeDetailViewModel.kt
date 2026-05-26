package xxx.yyy.zzz.feature.home.impl

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDetailUiState())
    val uiState: StateFlow<HomeDetailUiState> = _uiState.asStateFlow()

    fun initialize(title: String) {
        _uiState.value = HomeDetailUiState(
            originalTitle = title,
            editedTitle = title
        )
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(editedTitle = newTitle) }
    }

    data class HomeDetailUiState(
        val originalTitle: String = "",
        val editedTitle: String = ""
    )
}
