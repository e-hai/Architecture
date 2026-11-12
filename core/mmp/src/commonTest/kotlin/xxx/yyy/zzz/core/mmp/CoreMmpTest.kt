package xxx.yyy.zzz.core.mmp

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * CoreMmp 门面契约单元测试。
 */
class CoreMmpTest {
    private val fakeDelegate = FakeMmpDelegate()

    @BeforeTest
    fun setUp() {
        CoreMmp.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CoreMmp.install(DefaultMmpDelegate)
    }

    @Test
    fun testDispatchesToDelegate() {
        CoreMmp.doInit("af_token_001")
        CoreMmp.setCustomerUserId("cust_88")
        CoreMmp.trackEvent(MmpEvents.PURCHASE, mapOf(MmpParams.SKU to PayConstants.SKU))

        assertEquals("af_token_001", fakeDelegate.initializedToken)
        assertEquals("cust_88", fakeDelegate.recordedCustomerUserId)
        assertEquals(1, fakeDelegate.trackedEvents.size)
        assertEquals(MmpEvents.PURCHASE, fakeDelegate.trackedEvents[0].name)
        assertEquals(PayConstants.SKU, fakeDelegate.trackedEvents[0].params[MmpParams.SKU])
    }

    private object PayConstants {
        const val SKU = "subs_pro"
    }
}
