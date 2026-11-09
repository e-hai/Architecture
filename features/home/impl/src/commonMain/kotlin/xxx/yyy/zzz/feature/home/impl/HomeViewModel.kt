package xxx.yyy.zzz.feature.home.impl

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.model.AppConfig

/**
 * 首页 ViewModel 状态机。
 */
class HomeViewModel(
    private val appConfig: AppConfig,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            HomeUiState(
                welcomeMessage = CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, "欢迎体验 KMP 现代化架构！"),
                isDebug = appConfig.isDebug,
            ),
        )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
