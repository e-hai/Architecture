package xxx.yyy.zzz.feature.home.impl

import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.AppEnvironment
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 首页 ViewModel 单元测试。
 */
class HomeViewModelTest {
    @Test
    fun testHomeViewModelInitialState() {
        val config = AppConfig(env = AppEnvironment.DEV, isDebug = true)
        val viewModel = HomeViewModel(config)
        val state = viewModel.uiState.value

        assertTrue(state.isDebug)
        assertTrue(state.welcomeMessage.isNotBlank())
    }
}
