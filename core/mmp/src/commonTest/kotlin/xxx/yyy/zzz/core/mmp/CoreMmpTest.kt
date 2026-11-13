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
        CoreMmp.trackRevenue(
            revenue = 9.99,
            currency = "USD",
            eventName = MmpEvents.PURCHASE,
            params = mapOf(MmpParams.SKU to PayConstants.SKU),
        )

        assertEquals("af_token_001", fakeDelegate.initializedToken)
        assertEquals("cust_88", fakeDelegate.recordedCustomerUserId)
        assertEquals(1, fakeDelegate.trackedEvents.size)
        assertEquals(MmpEvents.PURCHASE, fakeDelegate.trackedEvents[0].name)
        assertEquals(PayConstants.SKU, fakeDelegate.trackedEvents[0].params[MmpParams.SKU])
        assertEquals(1, fakeDelegate.trackedRevenues.size)
        assertEquals(9.99, fakeDelegate.trackedRevenues[0].revenue)
        assertEquals("USD", fakeDelegate.trackedRevenues[0].currency)
    }

    @Test
    fun testAttributionCacheAndDeepLinkReplay() {
        val attribution =
            MmpAttribution(
                network = "googleadwords_int",
                campaign = "spring_sale",
                isOrganic = false,
            )
        fakeDelegate.emitAttribution(attribution)
        assertEquals(attribution, CoreMmp.getAttribution())

        var receivedAttribution: MmpAttribution? = null
        CoreMmp.setAttributionListener { receivedAttribution = it }
        assertEquals(attribution, receivedAttribution)

        val deepLink =
            MmpDeepLink(
                deepLinkValue = "myapp://promo",
                params = mapOf("coupon" to "VIP50"),
                isDeferred = false,
            )
        fakeDelegate.emitDeepLink(deepLink)
        var receivedDeepLink: MmpDeepLink? = null
        CoreMmp.setDeepLinkListener { receivedDeepLink = it }
        assertEquals(deepLink, receivedDeepLink)
    }

    private object PayConstants {
        const val SKU = "subs_pro"
    }
}
