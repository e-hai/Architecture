package xxx.yyy.zzz.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private data object TestKeyA : NavKey

private data object TestKeyB : NavKey

private data object TestSubKey1 : NavKey

private data object TestSubKey2 : NavKey

/**
 * Navigator 跨端路由导航控制器单元测试。
 * 验证一级栈、二级栈导航跳转、去重与多层回退机制。
 */
class NavigatorTest {
    @Test
    fun testNavigationStackAndBackFlow() {
        val state = NavigationState(startKey = TestKeyA, topLevelKeys = setOf(TestKeyA, TestKeyB))
        val navigator = Navigator(state)

        assertTrue(state.isAtRoot)
        assertEquals(TestKeyA, state.currentKey)

        // 导航至二级页面
        navigator.navigate(TestSubKey1)
        assertFalse(state.isAtRoot)
        assertEquals(TestSubKey1, state.currentKey)

        // 导航至另一二级页面并回退
        navigator.navigate(TestSubKey2)
        assertEquals(TestSubKey2, state.currentKey)
        navigator.goBack()
        assertEquals(TestSubKey1, state.currentKey)
        navigator.goBack()
        assertEquals(TestKeyA, state.currentKey)
        assertTrue(state.isAtRoot)

        // 顶级 Tab 切换与回退
        navigator.navigate(TestKeyB)
        assertEquals(TestKeyB, state.currentKey)
        navigator.goBack()
        assertEquals(TestKeyA, state.currentKey)
    }
}
