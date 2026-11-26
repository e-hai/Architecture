package xxx.yyy.zzz.feature.home.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.abtesting.DefaultAbTestingDelegate
import xxx.yyy.zzz.core.abtesting.FakeAbTestingDelegate
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.ui.MyProjectTheme
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * HomeScreen 跨平台 Compose UI 交互测试（覆盖调试入口显隐与 A/B 欢迎语渲染）。
 */
@OptIn(ExperimentalTestApi::class)
class HomeScreenUiTest : PlatformComposeUiTest() {
    private val fakeAbTesting = FakeAbTestingDelegate()

    @BeforeTest
    fun setUp() {
        CoreAbTesting.install(fakeAbTesting)
    }

    @AfterTest
    fun tearDown() {
        fakeAbTesting.clear()
        CoreAbTesting.install(DefaultAbTestingDelegate)
    }

    @Test
    fun testHomeScreenShowsWelcomeAndTriggersDebugNavigationInDebugMode() =
        runComposeUiTest {
            fakeAbTesting.values[AbTestingKeys.WELCOME_MESSAGE] = "Hello KMP UI Test"
            val viewModel = HomeViewModel(AppConfig(isDebug = true))
            var navigated = 0

            setContent {
                MyProjectTheme {
                    HomeScreen(
                        onNavigateToDebug = { navigated += 1 },
                        viewModel = viewModel,
                    )
                }
            }

            onNodeWithTag("home_title").assertIsDisplayed()
            onNodeWithText("Hello KMP UI Test").assertIsDisplayed()
            onNodeWithTag("home_open_debug_button").assertIsDisplayed().performClick()
            assertEquals(1, navigated)
        }

    @Test
    fun testHomeScreenHidesDebugButtonInReleaseMode() =
        runComposeUiTest {
            val viewModel = HomeViewModel(AppConfig(isDebug = false))

            setContent {
                MyProjectTheme {
                    HomeScreen(
                        onNavigateToDebug = {},
                        viewModel = viewModel,
                    )
                }
            }

            onNodeWithTag("home_title").assertIsDisplayed()
            onNodeWithTag("home_open_debug_button").assertDoesNotExist()
        }
}
