package xxx.yyy.zzz.shared.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.core.module.Module
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.navigation.NavKey
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.feature.debug.api.DebugNavKey
import xxx.yyy.zzz.feature.debug.impl.DebugScreen
import xxx.yyy.zzz.feature.debug.impl.debugModule

/**
 * 调试控制台跨端装配桥接（Debug 面板已启用分支）。
 * 当启用 `enableDebugPanel` 且工程包含 `:features:debug` 模块时编译此实现。
 */
internal object DebugPanelBridge {
    /** 当前分支是否已集成调试控制台。 */
    const val IS_ENABLED: Boolean = true

    /** 需要注入 Koin 容器的调试模块列表。 */
    val koinModules: List<Module> = listOf(debugModule)

    /** 构造从首页等业务页面跳转至调试控制台的回调。 */
    fun createNavigateAction(navigator: Navigator): (() -> Unit)? =
        {
            navigator.navigate(DebugNavKey)
        }

    /** 判断给定路由键是否为调试控制台路由。 */
    fun isDebugKey(key: NavKey): Boolean = key is DebugNavKey

    /** 呈现调试控制台页面。 */
    @Composable
    fun DebugDestination(
        navigator: Navigator,
        modifier: Modifier = Modifier,
    ) {
        DebugScreen(
            onBack = {
                CoreLog.d(LogTags.NAV, "Go back from Debug")
                navigator.goBack()
            },
            modifier = modifier,
        )
    }
}
