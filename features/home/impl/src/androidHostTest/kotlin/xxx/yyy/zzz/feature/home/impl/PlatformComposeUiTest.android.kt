package xxx.yyy.zzz.feature.home.impl

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROBOLECTRIC_SDK = 34

/**
 * Android Host JVM 侧 Compose UI 测试基类，通过 Robolectric 提供 UI 渲染与资源环境。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
actual abstract class PlatformComposeUiTest {
    @Before
    fun initComposeResourcesContext() {
        val providerClass = Class.forName("org.jetbrains.compose.resources.AndroidContextProvider")
        val companion = providerClass.getField("Companion").get(null)
        val setter = companion.javaClass.getMethod("setANDROID_CONTEXT", Context::class.java)
        setter.invoke(companion, InstrumentationRegistry.getInstrumentation().targetContext)
    }
}
