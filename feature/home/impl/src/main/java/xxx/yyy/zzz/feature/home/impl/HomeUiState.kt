package xxx.yyy.zzz.feature.home.impl

import xxx.yyy.zzz.core.model.User

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val user: User) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
