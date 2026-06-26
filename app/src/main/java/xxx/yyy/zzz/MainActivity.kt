package xxx.yyy.zzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.navigation.rememberNavigationState
import xxx.yyy.zzz.core.ui.MyProjectTheme
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.settings.api.SettingsNavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 开启全面屏，让系统状态栏和导航栏变为完全透明且去遮罩
        enableEdgeToEdge()

        setContent {
            val navigationState =
                rememberNavigationState(
                    startKey = HomeNavKey,
                    topLevelKeys = setOf(HomeNavKey, SettingsNavKey),
                )
            val navigator = remember(navigationState) { Navigator(navigationState) }

            MyProjectTheme {
                AppNavGraph(
                    navigationState = navigationState,
                    navigator = navigator,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
