package xxx.yyy.zzz.core.ui

import xxx.yyy.zzz.core.ui.generated.resources.Res
import xxx.yyy.zzz.core.ui.generated.resources.app_name
import xxx.yyy.zzz.core.ui.generated.resources.back
import xxx.yyy.zzz.core.ui.generated.resources.clear_log
import xxx.yyy.zzz.core.ui.generated.resources.debug_console_title
import xxx.yyy.zzz.core.ui.generated.resources.open_debug_console
import xxx.yyy.zzz.core.ui.generated.resources.sub_title
import xxx.yyy.zzz.core.ui.generated.resources.welcome_message
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * 设计系统色板令牌与多语言资源描述符校验测试。
 */
class CoreUiThemeTest {
    @Test
    fun testThemeColorTokensDistinctAndOpaque() {
        val palette = listOf(Purple80, PurpleGrey80, Pink80, Purple40, PurpleGrey40, Pink40)
        assertEqualsSize(6, palette.toSet().size)
        palette.forEach { color ->
            assertTrue(color.alpha == 1f, "Expected opaque theme token, got $color")
        }
        assertNotEquals(Purple80, Purple40)
    }

    @Test
    fun testCoreUiStringResourceKeysGenerated() {
        val keys =
            listOf(
                Res.string.app_name,
                Res.string.welcome_message,
                Res.string.sub_title,
                Res.string.back,
                Res.string.open_debug_console,
                Res.string.debug_console_title,
                Res.string.clear_log,
            )
        assertEqualsSize(7, keys.toSet().size)
    }

    private fun assertEqualsSize(
        expected: Int,
        actual: Int,
    ) {
        assertTrue(expected == actual, "Expected $expected unique items, got $actual")
    }
}
