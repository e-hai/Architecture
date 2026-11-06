package xxx.yyy.zzz.core.log

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 契约驱动测试：验证 CoreLog 门面在注入 FakeLogDelegate 后的拦截与分发行为。
 */
class CoreLogTest {
    private val fakeDelegate = FakeLogDelegate()

    @BeforeTest
    fun setUp() {
        CoreLog.install(fakeDelegate)
        fakeDelegate.clear()
    }

    @AfterTest
    fun tearDown() {
        CoreLog.install(DefaultLogDelegate)
    }

    @Test
    fun testCoreLogDispatchesToDelegate() {
        CoreLog.d("TEST_TAG", "Debug message")
        CoreLog.i("TEST_TAG", "Info message")
        CoreLog.w("TEST_TAG", "Warn message")
        CoreLog.e("TEST_TAG", "Error message")

        assertEquals(4, fakeDelegate.logs.size)
        assertEquals(FakeLogDelegate.LogEntry("DEBUG", "TEST_TAG", "Debug message"), fakeDelegate.logs[0])
        assertEquals(FakeLogDelegate.LogEntry("INFO", "TEST_TAG", "Info message"), fakeDelegate.logs[1])
        assertEquals(FakeLogDelegate.LogEntry("WARN", "TEST_TAG", "Warn message"), fakeDelegate.logs[2])
        assertEquals(FakeLogDelegate.LogEntry("ERROR", "TEST_TAG", "Error message"), fakeDelegate.logs[3])
    }
}
