package xxx.yyy.zzz.core.crashreport

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * CoreCrashReport 门面契约单元测试。
 */
class CoreCrashReportTest {
    private val fakeDelegate = FakeCrashReportDelegate()

    @BeforeTest
    fun setUp() {
        CoreCrashReport.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CoreCrashReport.install(DefaultCrashReportDelegate)
    }

    @Test
    fun testDispatchesToDelegate() {
        CoreCrashReport.doInit()
        CoreCrashReport.setUserId("user_42")
        CoreCrashReport.setCustomKey(CrashReportKeys.IS_DEBUG, true)
        CoreCrashReport.log("entered_checkout")
        val error = IllegalStateException("boom")
        CoreCrashReport.recordException(error)

        assertEquals(1, fakeDelegate.initCount)
        assertEquals("user_42", fakeDelegate.currentUserId)
        assertEquals(true, fakeDelegate.customKeys[CrashReportKeys.IS_DEBUG])
        assertEquals(listOf("entered_checkout"), fakeDelegate.breadcrumbs)
        assertEquals<List<Throwable>>(listOf(error), fakeDelegate.exceptions)
    }
}
