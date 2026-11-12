package xxx.yyy.zzz.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import xxx.yyy.zzz.core.navigation.NavKey
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.ui.MyProjectTheme
import xxx.yyy.zzz.shared.navigation.AppNavGraph
import xxx.yyy.zzz.shared.navigation.defaultStartKey
import xxx.yyy.zzz.shared.navigation.defaultTopLevelKeys

/**
 * 跨平台应用 Compose 根入口视图。
 * 与具体业务 Feature 解耦，起始路由通过 [defaultStartKey] 统一装配。
 */
@Composable
fun App(
    startKey: NavKey = defaultStartKey,
    topLevelKeys: Set<NavKey> = defaultTopLevelKeys,
) {
    MyProjectTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            val navState =
                remember(startKey, topLevelKeys) {
                    NavigationState(
                        startKey = startKey,
                        topLevelKeys = topLevelKeys,
                    )
                }
            val navigator = remember(navState) { Navigator(navState) }
            AppNavGraph(
                navigator = navigator,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}
