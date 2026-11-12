package xxx.yyy.zzz.core.pay

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * CorePay 门面契约单元测试。
 */
class CorePayTest {
    private val fakeDelegate = FakePayDelegate()

    @BeforeTest
    fun setUp() {
        CorePay.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CorePay.install(DefaultPayDelegate)
    }

    @Test
    fun testPurchaseAndSubscriptionLifecycle() {
        CorePay.doInit()
        assertEquals(1, fakeDelegate.initCount)
        assertFalse(CorePay.isSubscribed(PayProducts.SUBS_PRO))

        var purchaseSuccess = false
        CorePay.purchase(PayProducts.SUBS_PRO) { success, _ ->
            purchaseSuccess = success
        }
        assertTrue(purchaseSuccess)
        assertTrue(CorePay.isSubscribed(PayProducts.SUBS_PRO))

        fakeDelegate.nextPurchaseSuccess = false
        fakeDelegate.nextPurchaseError = "User cancelled"
        var failureMessage: String? = null
        CorePay.purchase(PayProducts.SUBS_PLUS) { success, error ->
            assertFalse(success)
            failureMessage = error
        }
        assertEquals("User cancelled", failureMessage)
        assertFalse(CorePay.isSubscribed(PayProducts.SUBS_PLUS))
    }
}
