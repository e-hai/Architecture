package xxx.yyy.zzz.shared.performance

import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.analytics.CoreAnalytics
import xxx.yyy.zzz.core.analytics.DefaultAnalyticsDelegate
import xxx.yyy.zzz.core.analytics.FakeAnalyticsDelegate
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.DefaultLogDelegate
import xxx.yyy.zzz.core.log.FakeLogDelegate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

/**
 * StartupPerformanceTracker 契约单元测试（Fake over Mock，覆盖阶段计时与幂等上报）。
 */
class StartupPerformanceTrackerTest {
    private val fakeAnalytics = FakeAnalyticsDelegate()
    private val fakeLog = FakeLogDelegate()
    private val testTimeSource = TestTimeSource()

    @BeforeTest
    fun setUp() {
        CoreAnalytics.install(fakeAnalytics)
        CoreLog.install(fakeLog)
        StartupPerformanceTracker.resetForTesting(testTimeSource)
    }

    @AfterTest
    fun tearDown() {
        StartupPerformanceTracker.resetForTesting()
        fakeAnalytics.clear()
        fakeLog.clear()
        CoreAnalytics.install(DefaultAnalyticsDelegate)
        CoreLog.install(DefaultLogDelegate)
    }

    @Test
    fun testTracksColdStartMilestonesAndReportsOnceIdempotently() {
        StartupPerformanceTracker.markProcessStart("ios")
        testTimeSource += 45.milliseconds
        StartupPerformanceTracker.markStage1EssentialReady()

        testTimeSource += 120.milliseconds
        assertTrue(StartupPerformanceTracker.markFirstFrameRendered())
        assertFalse(StartupPerformanceTracker.markFirstFrameRendered())

        assertEquals(1, fakeAnalytics.events.size)
        val tracked = fakeAnalytics.events.single()
        assertEquals(AnalyticsEvents.App.COLD_START, tracked.name)
        assertEquals("ios", tracked.params["platform"])
        assertEquals(45L, tracked.params["stage1_ms"])
        assertEquals(165L, tracked.params["ttid_ms"])

        assertTrue(fakeLog.logs.any { it.message.contains("ttid=165ms, stage1=45ms") })
    }

    @Test
    fun testMarkFirstFrameWithoutProcessStartIsNoOp() {
        assertFalse(StartupPerformanceTracker.markFirstFrameRendered())
        assertTrue(fakeAnalytics.events.isEmpty())
    }
}
