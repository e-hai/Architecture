package xxx.yyy.zzz.feature.debug.impl

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.DefaultLogDelegate
import xxx.yyy.zzz.core.log.FakeLogDelegate
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.AppEnvironment
import xxx.yyy.zzz.core.ui.MyProjectTheme
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeEmitLogSection : DebugSection {
    override val id: String = "fake_ui_probe"
    override val category: DebugCategory = DebugCategory.MEDIA_ANIMATION
    override val order: Int = 0

    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Button(
            onClick = { emitLog("TEST", "ping-from-ui-test", "I") },
            modifier = Modifier.testTag("fake_emit_log_button"),
        ) {
            Text("Emit Probe Log")
        }
    }
}

/**
 * DebugScreen 跨平台 Compose UI 交互测试（覆盖环境卡片展示、日志回显与清空、返回回调）。
 */
@OptIn(ExperimentalTestApi::class)
class DebugScreenUiTest : PlatformComposeUiTest() {
    private val fakeLog = FakeLogDelegate()

    @BeforeTest
    fun setUp() {
        CoreLog.install(fakeLog)
        runCatching { stopKoin() }
        startKoin {
            modules(
                module {
                    single {
                        AppConfig(
                            env = AppEnvironment.DEV,
                            isDebug = true,
                            baseUrl = "https://api.test.local",
                        )
                    }
                    single { FakeEmitLogSection() } bind DebugSection::class
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        runCatching { stopKoin() }
        fakeLog.clear()
        CoreLog.install(DefaultLogDelegate)
    }

    @Test
    fun testDebugScreenEmitsClearsLogsAndInvokesBack() =
        runComposeUiTest {
            var backClicks = 0
            setContent {
                MyProjectTheme {
                    DebugScreen(onBack = { backClicks += 1 })
                }
            }

            onNodeWithTag("debug_console_card").assertIsDisplayed()
            onNodeWithText("• 服务端基地址: https://api.test.local").assertIsDisplayed()

            onNodeWithTag("debug_lazy_list").performScrollToNode(hasTestTag("debug_category_MEDIA_ANIMATION"))
            onNodeWithTag("fake_emit_log_button").performClick()
            assertTrue(fakeLog.logs.any { it.tag == "TEST" && it.message == "ping-from-ui-test" })

            onNodeWithTag("debug_lazy_list").performScrollToIndex(0)
            onNodeWithText("[TEST][I] ping-from-ui-test").assertIsDisplayed()

            onNodeWithTag("debug_clear_logs_button").performClick()
            onNodeWithText("[TEST][I] ping-from-ui-test").assertDoesNotExist()

            onNodeWithTag("debug_back_button").performClick()
            assertEquals(1, backClicks)
        }
}
