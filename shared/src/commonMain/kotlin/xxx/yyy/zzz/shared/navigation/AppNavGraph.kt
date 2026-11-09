@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.navigation.Navigator
// [FEATURE_IMPORTS_START]
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.impl.HomeScreen
// [FEATURE_IMPORTS_END]
import xxx.yyy.zzz.shared.debug.DebugScreen

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
        is HomeNavKey -> {
            HomeScreen(
                onNavigateToDebug = { navigator.navigate(DebugNavKey) },
                modifier = modifier,
            )
        }

        // [FEATURE_NAV_CASES_END]

        is DebugNavKey -> {
            DebugScreen(
                onBack = {
                    CoreLog.d(LogTags.NAV, "Go back from Debug")
                    navigator.goBack()
                },
                modifier = modifier,
            )
        }

        else -> {
            HomeScreen(
                onNavigateToDebug = { navigator.navigate(DebugNavKey) },
                modifier = modifier,
            )
        }
    }
}
