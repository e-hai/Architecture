package xxx.yyy.zzz

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.navigation.toEntries

/**
 * 骨架导航图。
 *
 * 演示 Navigation3 的 [NavDisplay] + [NavigationState.toEntries] + entryProvider 模式。
 * 开发真实项目时：
 *   1. 移除 [SkeletonNavKey]，替换为各个 feature 的 NavKey
 *   2. 将 entryProvider 替换为真实的 [entryProvider] DSL 调用
 *   3. 按需添加 Scaffold/TopAppBar/BottomBar
 */
@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    /*
     * 正式用法（添加 feature 模块后启用）：
     *
     * val entryProvider = entryProvider {
     *     featureOneEntry(
     *         onNavigate = { navigator.navigate(it) },
     *         onBack = { navigator.goBack() },
     *     )
     *     featureTwoEntry(
     *         onNavigate = { navigator.navigate(it) },
     *         onBack = { navigator.goBack() },
     *     )
     * }
     *
     * Scaffold { innerPadding ->
     *     NavDisplay(
     *         entries = navigationState.toEntries(entryProvider),
     *         onBack = { navigator.goBack() },
     *         modifier = Modifier.padding(innerPadding),
     *     )
     * }
     */

    // 骨架占位：直接渲染单个占位页面
    skeletonEntry(modifier)
}

@Composable
private fun skeletonEntry(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.app_name))
    }
}

/**
 * entryProvider 占位实现。
 *
 * 开发真实项目时替换为由 [entryProvider] DSL 生成的 provider，签名与 [NavigationState.toEntries]
 * 的 entryProvider 参数兼容。
 */
private val skeletonEntryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
    error("SkeletonApp 不应触发导航。请替换 feature NavKey 后实现真实 entryProvider。key=$key")
}
