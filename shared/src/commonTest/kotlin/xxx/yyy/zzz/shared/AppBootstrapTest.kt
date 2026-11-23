@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared

import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
// [KIT:mmp:START]
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.mmp.DefaultMmpDelegate
import xxx.yyy.zzz.core.mmp.FakeMmpDelegate
import xxx.yyy.zzz.core.mmp.MmpDeepLink
import xxx.yyy.zzz.shared.navigation.defaultStartKey
// [KIT:mmp:END]
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
            AppBootstrap.resetForTesting()
        }
    }

    @Test
    fun testDeferredKitInitializerIdempotency() {
        var triggerCount = 0
        try {
            AppBootstrap.resetForTesting()
            assertFalse(AppBootstrap.isDeferredKitsInitialized())
            assertFalse(AppBootstrap.doInitDeferredKits())

            AppBootstrap.registerDeferredKitInitializer {
                triggerCount += 1
            }
            assertTrue(AppBootstrap.doInitDeferredKits())
            assertTrue(AppBootstrap.isDeferredKitsInitialized())
            assertFalse(AppBootstrap.doInitDeferredKits())
            assertEquals(1, triggerCount)
        } finally {
            AppBootstrap.resetForTesting()
        }
    }

    // [KIT:mmp:START]
    @Test
    fun testDeepLinkResolutionAndMmpDispatch() {
        assertEquals(
            defaultStartKey,
            resolveDeepLinkNavKey(MmpDeepLink(deepLinkValue = "myapp://home?coupon=VIP50")),
        )
        assertEquals(
            DebugPanelBridge.debugKey,
            resolveDeepLinkNavKey(MmpDeepLink(params = mapOf("route" to "/debug"))),
        )
        assertNull(resolveDeepLinkNavKey(MmpDeepLink(deepLinkValue = "myapp://unknown_screen")))

        val fakeMmp = FakeMmpDelegate()
        CoreMmp.install(fakeMmp)
        val navState = NavigationState(startKey = HomeNavKey, topLevelKeys = setOf(HomeNavKey))
        val navigator = Navigator(navState)
        try {
            fakeMmp.emitDeepLink(MmpDeepLink(deepLinkValue = "myapp://debug", isDeferred = true))
            CoreMmp.setDeepLinkListener { deepLink ->
                resolveDeepLinkNavKey(deepLink)?.let(navigator::navigate)
            }
            assertEquals(DebugPanelBridge.IS_ENABLED, DebugPanelBridge.isDebugKey(navigator.state.currentKey))
        } finally {
            CoreMmp.setDeepLinkListener(null)
            CoreMmp.install(DefaultMmpDelegate)
        }
    }
    // [KIT:mmp:END]
}
