@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.navigation.NavKey
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.ui.ErrorView
// [FEATURE_IMPORTS_START]
// [FEATURE:home:START]
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.impl.HomeScreen
// [FEATURE:home:END]
// [FEATURE:debug:START]
import xxx.yyy.zzz.feature.debug.api.DebugNavKey
import xxx.yyy.zzz.feature.debug.impl.DebugScreen
// [FEATURE:debug:END]
// [FEATURE_IMPORTS_END]

// [START_ROUTE_START]

/** 默认应用起始根路由 */
val defaultStartKey: NavKey = HomeNavKey

/** 默认顶级导航根路由集合 */
val defaultTopLevelKeys: Set<NavKey> = setOf(HomeNavKey)
// [START_ROUTE_END]

/**
 * 跨平台路由分发图。
 */
@Composable
fun AppNavGraph(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val currentKey = navigator.state.currentKey

    when (currentKey) {
        // [FEATURE_NAV_CASES_START]
        // [FEATURE:home:START]
        is HomeNavKey -> {
            HomeScreen(
                onNavigateToDebug = { navigator.navigate(DebugNavKey) },
                modifier = modifier,
            )
        }

        // [FEATURE:home:END]
        // [FEATURE:debug:START]
        is DebugNavKey -> {
            DebugScreen(
                onBack = {
                    CoreLog.d(LogTags.NAV, "Go back from Debug")
                    navigator.goBack()
                },
                modifier = modifier,
            )
        }

        // [FEATURE:debug:END]
        // [FEATURE_NAV_CASES_END]

        else -> {
            ErrorView(
                message = "未知页面路由: $currentKey",
                retryText = "返回首页",
                onRetry = { navigator.navigate(defaultStartKey) },
                modifier = modifier,
            )
        }
    }
}
