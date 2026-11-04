package xxx.yyy.zzz.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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
        Surface(modifier = Modifier.fillMaxSize()) {
            val navState =
                remember {
                    NavigationState(
                        startKey = HomeNavKey,
                        topLevelKeys = setOf(HomeNavKey, SettingsNavKey),
                    )
                }
            val navigator = remember { Navigator(navState) }
            AppNavGraph(navigator = navigator)
        }
    }
}
