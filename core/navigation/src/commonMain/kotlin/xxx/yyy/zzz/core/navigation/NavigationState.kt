package xxx.yyy.zzz.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * 跨平台导航状态承载者，管理顶级页面栈与二级子栈。
 *
 * @property startKey 应用默认起始根路由 Key
 * @property topLevelKeys 所有属于一级底部导航栏的根路由集合
 */
class NavigationState(
    val startKey: NavKey,
    val topLevelKeys: Set<NavKey>,
) {
    val topLevelStack: SnapshotStateList<NavKey> = mutableStateListOf(startKey)
    private val subStacks: MutableMap<NavKey, SnapshotStateList<NavKey>> = mutableMapOf()

    val currentTopLevelKey: NavKey
        get() = topLevelStack.lastOrNull() ?: startKey

    val currentSubStack: SnapshotStateList<NavKey>
        get() =
            subStacks.getOrPut(currentTopLevelKey) {
                mutableStateListOf(currentTopLevelKey)
            }

    val currentKey: NavKey
        get() = currentSubStack.lastOrNull() ?: currentTopLevelKey

    val isAtRoot: Boolean
        get() = topLevelStack.size <= 1 && currentSubStack.size <= 1
}
