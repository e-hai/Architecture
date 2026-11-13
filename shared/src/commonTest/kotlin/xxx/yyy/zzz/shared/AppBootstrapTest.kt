package xxx.yyy.zzz.shared

import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.AppEnvironment
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.network.NetworkDataSource
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.impl.HomeViewModel
import xxx.yyy.zzz.shared.debug.DebugPanelBridge
import xxx.yyy.zzz.shared.di.AppBootstrap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Shared 装配中枢与 Debug 裁剪桥接集成测试。
 */
class AppBootstrapTest {
    @Test
    fun testKoinBootstrapAndDebugPanelBridge() {
        val config = AppConfig(env = AppEnvironment.DEV, isDebug = true)
        try {
            AppBootstrap.doInitKoin(appConfig = config)
            val koin = KoinPlatform.getKoin()
            assertEquals(config, koin.get<AppConfig>())
            assertNotNull(koin.get<NetworkDataSource>())
            assertNotNull(koin.get<HomeViewModel>())

            val navState = NavigationState(startKey = HomeNavKey, topLevelKeys = setOf(HomeNavKey))
            val navigator = Navigator(navState)
            val navigateAction = DebugPanelBridge.createNavigateAction(navigator)

            if (DebugPanelBridge.IS_ENABLED) {
                assertTrue(DebugPanelBridge.koinModules.isNotEmpty())
                assertNotNull(navigateAction)
                navigateAction()
                assertTrue(DebugPanelBridge.isDebugKey(navigator.state.currentKey))
            } else {
                assertTrue(DebugPanelBridge.koinModules.isEmpty())
                assertNull(navigateAction)
                assertFalse(DebugPanelBridge.isDebugKey(navigator.state.currentKey))
            }
        } finally {
            stopKoin()
        }
    }
}
