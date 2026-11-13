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
        val catalog =
            PayProductCatalog.fromCsv(
                subsCsv = "${PayProducts.SUBS_PLUS}, ${PayProducts.SUBS_PRO}",
                consumableCsv = PayProducts.CONSUMABLE_SKIN_3DAY,
                nonConsumableCsv = PayProducts.NON_CONSUMABLE_SKIN,
            )
        CorePay.doInit(catalog)
        assertEquals(1, fakeDelegate.initCount)
        assertEquals(catalog, CorePay.getCatalog())
        assertFalse(CorePay.isSubscribed(PayProducts.SUBS_PRO))

        fakeDelegate.stubbedProducts.add(
            PayProductInfo(
                productId = PayProducts.SUBS_PRO,
                type = PayProductType.SUBS,
                title = "Pro Monthly",
                description = "Full access",
                formattedPrice = "$9.99",
                priceAmountMicros = 9_990_000L,
                priceCurrencyCode = "USD",
                subscriptionPeriod = "P1M",
            ),
        )
        var fetched: List<PayProductInfo> = emptyList()
        CorePay.fetchProducts(catalog.subsProductIds) { products, error ->
            assertEquals(null, error)
            fetched = products
        }
        assertEquals(1, fetched.size)
        assertEquals("$9.99", fetched.first().formattedPrice)

        var purchaseSuccess = false
        CorePay.purchase(PayProducts.SUBS_PRO) { success, _ ->
            purchaseSuccess = success
        }
        assertTrue(purchaseSuccess)
        assertTrue(CorePay.isSubscribed(PayProducts.SUBS_PRO))

        var restoreSuccess = false
        CorePay.restorePurchases { success, _ ->
            restoreSuccess = success
        }
        assertTrue(restoreSuccess)

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
