// 裁剪分支桩实现需与 debugEnabled 保持完全相同的函数签名并返回固定值，故意忽略参数
@file:Suppress("FunctionOnlyReturningConstant", "UnusedParameter")

package xxx.yyy.zzz.shared.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.core.module.Module
import xxx.yyy.zzz.core.navigation.NavKey
import xxx.yyy.zzz.core.navigation.Navigator

/**
 * 调试控制台跨端装配桥接（Prod / 裁剪空实现分支）。
 * 当传入 `-PenableDebugPanel=false` 或执行 `./kmp feature remove debug` 卸载调试模块时编译此分支，
 * 确保正式发布包零调试面板代码与零资产残留。
 */
internal object DebugPanelBridge {
    /** 当前分支是否已集成调试控制台。 */
    const val IS_ENABLED: Boolean = false

    /** 调试控制台路由键（裁剪态恒为 null）。 */
    val debugKey: NavKey? = null

    /** 需要注入 Koin 容器的调试模块列表（裁剪态为空）。 */
    val koinModules: List<Module> = emptyList()

    /** 返回 null，使宿主首页不渲染调试台入口按钮。 */
    fun createNavigateAction(navigator: Navigator): (() -> Unit)? = null

    /** 判断给定路由键是否为调试控制台路由（裁剪态恒为 false）。 */
    fun isDebugKey(key: NavKey): Boolean = false

    /** 空占位目标呈现函数。 */
    @Composable
    fun DebugDestination(
        navigator: Navigator,
        modifier: Modifier = Modifier,
    ) = Unit
}
