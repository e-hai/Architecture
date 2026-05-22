package xxx.yyy.zzz.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.domain.GetUserUseCase
import xxx.yyy.zzz.core.domain.UserRepository

class HomeViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getUserUseCase("1")
        .map { user ->
            if (user != null) {
                HomeUiState.Success(user)
            } else {
                HomeUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onSyncClick() {
        viewModelScope.launch {
            userRepository.syncUser("1")
        }
    }
}
