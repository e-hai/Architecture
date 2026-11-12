package xxx.yyy.zzz.core.analytics

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * CoreAnalytics 门面契约单元测试。
 */
class CoreAnalyticsTest {
    private val fakeDelegate = FakeAnalyticsDelegate()

    @BeforeTest
    fun setUp() {
        CoreAnalytics.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CoreAnalytics.install(DefaultAnalyticsDelegate)
    }

    @Test
    fun testDispatchesToDelegate() {
        CoreAnalytics.doInit()
        CoreAnalytics.setUserId("u_1001")
        CoreAnalytics.setUserProperty("tier", "pro")
        CoreAnalytics.logEvent(
            AnalyticsEvents.Common.BUTTON_CLICK,
            mapOf(AnalyticsParams.ITEM_ID to "upgrade_btn"),
        )

        assertEquals(1, fakeDelegate.initCount)
        assertEquals("u_1001", fakeDelegate.currentUserId)
        assertEquals("pro", fakeDelegate.userProperties["tier"])
        assertEquals(1, fakeDelegate.events.size)
        assertEquals(AnalyticsEvents.Common.BUTTON_CLICK, fakeDelegate.events[0].name)
        assertEquals("upgrade_btn", fakeDelegate.events[0].params[AnalyticsParams.ITEM_ID])
    }
}
