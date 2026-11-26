package xxx.yyy.zzz.shared.crash

import xxx.yyy.zzz.core.crashreport.CoreCrashReport
import xxx.yyy.zzz.core.crashreport.CrashReportDelegate
import xxx.yyy.zzz.core.crashreport.DefaultCrashReportDelegate
import xxx.yyy.zzz.core.crashreport.FakeCrashReportDelegate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * IosCrashHook 契约单元测试（Fake over Mock，运行于 iOS Simulator 平台）。
 */
class IosCrashHookTest {
    private val fakeCrashReport = FakeCrashReportDelegate()

    @BeforeTest
    fun setUp() {
        CoreCrashReport.install(fakeCrashReport)
        IosCrashHook.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        fakeCrashReport.clear()
        CoreCrashReport.install(DefaultCrashReportDelegate)
        IosCrashHook.resetForTesting()
    }

    @Test
    fun testHandleUnhandledExceptionRecordsMetadataAndInvokesTerminator() {
        val boom = IllegalStateException("kmp-ios-crash-probe")
        var terminatedWith: Throwable? = null

        IosCrashHook.handleUnhandledException(boom) { err ->
            terminatedWith = err
        }

        assertSame(boom, terminatedWith)
        assertTrue(fakeCrashReport.customKeys["kotlin_exception_type"]?.toString()?.contains("IllegalStateException") == true)
        assertTrue(fakeCrashReport.breadcrumbs.any { it.contains("kmp-ios-crash-probe") })
        assertEquals<List<Throwable>>(listOf(boom), fakeCrashReport.exceptions)
    }

    @Test
    fun testHandleUnhandledExceptionStillTerminatesWhenReporterThrows() {
        CoreCrashReport.install(
            object : CrashReportDelegate {
                override fun doInit() = Unit

                override fun setUserId(userId: String) = Unit

                override fun setCustomKey(
                    key: String,
                    value: Any,
                ) = Unit

                override fun log(message: String) = Unit

                override fun recordException(throwable: Throwable) {
                    error("reporter-failure")
                }
            },
        )
        val boom = RuntimeException("fatal-kmp-error")
        var terminatedWith: Throwable? = null

        IosCrashHook.handleUnhandledException(boom) { err ->
            terminatedWith = err
        }

        assertSame(boom, terminatedWith)
    }

    @Test
    fun testFormatStackTraceRespectsMaxLength() {
        val hugeMessage = "X".repeat(6000)
        val formatted = IosCrashHook.formatStackTrace(IllegalArgumentException(hugeMessage))
        assertTrue(formatted.length <= 4096)
        assertTrue(formatted.contains("IllegalArgumentException"))
    }
}
