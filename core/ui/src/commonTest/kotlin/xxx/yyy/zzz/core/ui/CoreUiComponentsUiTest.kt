package xxx.yyy.zzz.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import xxx.yyy.zzz.core.ui.nativehost.NativeViewHost
import xxx.yyy.zzz.core.ui.nativehost.NativeViewRegistry
import xxx.yyy.zzz.core.ui.nativehost.NativeViewRequest
import xxx.yyy.zzz.core.ui.nativehost.ScopedNativeHandle
import xxx.yyy.zzz.core.ui.nativehost.rememberScopedNativeHandle
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `:core:ui` 核心 UI 组件与宿主容器跨平台 Compose UI 交互测试。
 */
@OptIn(ExperimentalTestApi::class)
class CoreUiComponentsUiTest : PlatformComposeUiTest() {
    @AfterTest
    fun tearDown() {
        NativeViewRegistry.clear()
    }

    @Test
    fun testErrorViewDisplaysMessageAndTriggersRetry() =
        runComposeUiTest {
            var retryCount = 0
            setContent {
                MyProjectTheme {
                    ErrorView(
                        message = "Network unavailable",
                        retryText = "Retry Now",
                        onRetry = { retryCount += 1 },
                    )
                }
            }

            onNodeWithTag("error_message").assertIsDisplayed()
            onNodeWithText("Network unavailable").assertIsDisplayed()
            onNodeWithTag("error_retry_button").assertIsDisplayed().performClick()
            assertEquals(1, retryCount)
        }

    @Test
    fun testErrorViewHidesRetryButtonWhenCallbackNull() =
        runComposeUiTest {
            setContent {
                MyProjectTheme {
                    ErrorView(
                        message = "Fatal configuration error",
                        onRetry = null,
                    )
                }
            }

            onNodeWithText("Fatal configuration error").assertIsDisplayed()
            onNodeWithTag("error_retry_button").assertDoesNotExist()
        }

    @Test
    fun testNativeViewHostRendersUnregisteredFallback() =
        runComposeUiTest {
            NativeViewRegistry.clear()
            setContent {
                MyProjectTheme {
                    NativeViewHost(
                        request = NativeViewRequest(viewType = "video_demo"),
                    )
                }
            }

            onNodeWithTag("native_view_fallback").assertIsDisplayed()
            onNodeWithText("Unregistered NativeView: video_demo").assertIsDisplayed()
        }

    @Test
    fun testRememberScopedNativeHandleDisposesWhenRemovedFromComposition() =
        runComposeUiTest {
            var mounted by mutableStateOf(true)
            var closeCount = 0
            var closedState = false
            val fakeHandle =
                object : ScopedNativeHandle {
                    override var isClosed: Boolean = false
                        private set

                    override fun close() {
                        if (isClosed) return
                        isClosed = true
                        closedState = true
                        closeCount += 1
                    }
                }

            setContent {
                if (mounted) {
                    rememberScopedNativeHandle(key = "probe") { fakeHandle }
                }
            }

            waitForIdle()
            assertEquals(0, closeCount)

            mounted = false
            waitForIdle()
            assertTrue(closedState)
            assertEquals(1, closeCount)
        }
}
