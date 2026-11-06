package xxx.yyy.zzz.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.ui.MyProjectTheme
import xxx.yyy.zzz.shared.navigation.AppNavGraph
import xxx.yyy.zzz.shared.navigation.HomeNavKey
import xxx.yyy.zzz.shared.navigation.SettingsNavKey

/**
 * 跨平台应用 Compose 根入口视图。
 */
@Composable
fun App() {
    MyProjectTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            val navState =
                remember {
                    NavigationState(
                        startKey = HomeNavKey,
                        topLevelKeys = setOf(HomeNavKey, SettingsNavKey),
                    )
                }
            val navigator = remember { Navigator(navState) }
            AppNavGraph(
                navigator = navigator,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}
