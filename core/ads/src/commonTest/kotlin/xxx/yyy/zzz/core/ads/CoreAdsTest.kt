package xxx.yyy.zzz.core.ads

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * CoreAds 门面契约单元测试。
 */
class CoreAdsTest {
    private val fakeDelegate = FakeAdsDelegate()

    @BeforeTest
    fun setUp() {
        CoreAds.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CoreAds.install(DefaultAdsDelegate)
    }

    @Test
    fun testAdLifecycleDispatchesToDelegate() {
        CoreAds.doInit("test_api_key")
        assertEquals("test_api_key", fakeDelegate.initializedApiKey)
        assertFalse(CoreAds.isReady(AdsKeys.Trigger.REWARD_VIDEO))

        CoreAds.loadAd(AdsKeys.Trigger.REWARD_VIDEO, "unit_123")
        assertTrue(CoreAds.isReady(AdsKeys.Trigger.REWARD_VIDEO))

        CoreAds.showAd(AdsKeys.Trigger.REWARD_VIDEO)
        assertEquals(listOf(AdsKeys.Trigger.REWARD_VIDEO), fakeDelegate.shownTriggers)
        assertFalse(CoreAds.isReady(AdsKeys.Trigger.REWARD_VIDEO))

        CoreAds.openDebug()
        assertEquals(1, fakeDelegate.debugOpenedCount)
    }
}
