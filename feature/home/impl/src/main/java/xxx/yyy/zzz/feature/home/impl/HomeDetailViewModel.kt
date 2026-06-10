package xxx.yyy.zzz.feature.home.impl

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDetailUiState())
    val uiState: StateFlow<HomeDetailUiState> = _uiState.asStateFlow()

    init {
        Log.d("HomeDetailViewModel", "init")
    }

    fun initialize(title: String) {
        _uiState.update {
            HomeDetailUiState(
                title = title
            )
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = newTitle
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("HomeDetailViewModel", "onCleared")
    }

    data class HomeDetailUiState(
        val title: String = ""
    )
}
