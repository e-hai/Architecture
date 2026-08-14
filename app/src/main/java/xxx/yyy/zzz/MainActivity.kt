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
import xxx.yyy.zzz.feature.discover.api.DiscoverNavKey
import xxx.yyy.zzz.feature.feed.api.FeedNavKey
import xxx.yyy.zzz.feature.profile.api.ProfileNavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 开启全面屏，让系统状态栏和导航栏变为完全透明且去遮罩
        enableEdgeToEdge()

        setContent {
            val feedKey = remember { FeedNavKey() }
            val discoverKey = remember { DiscoverNavKey() }
            val profileKey = remember { ProfileNavKey() }
            val topLevelKeys = remember { setOf(feedKey, discoverKey, profileKey) }

            val navigationState =
                rememberNavigationState(
                    startKey = feedKey,
                    topLevelKeys = topLevelKeys,
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
