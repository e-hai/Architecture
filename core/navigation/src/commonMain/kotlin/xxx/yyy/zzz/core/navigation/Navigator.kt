package xxx.yyy.zzz.core.navigation

/**
 * 跨平台导航控制器，响应路由跳转与页面回退。
 *
 * @property state 导航状态对象
 */
class Navigator(
    val state: NavigationState,
) {
    /**
     * 跳转至指定页面。
     *
     * @param key 目标路由键
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    /**
     * 页面回退。
     */
    fun goBack() {
        if (state.isAtRoot) return

        when (state.currentKey) {
            state.startKey -> {
                if (state.topLevelStack.size > 1) {
                    state.topLevelStack.removeLastOrNull()
                }
            }

            state.currentTopLevelKey -> {
                state.topLevelStack.removeLastOrNull()
            }

            else -> {
                state.currentSubStack.removeLastOrNull()
            }
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
