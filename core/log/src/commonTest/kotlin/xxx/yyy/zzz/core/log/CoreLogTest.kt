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
        CoreLog.v("TEST_TAG", "Verbose message")
        CoreLog.d("TEST_TAG", "Debug message")
        CoreLog.i("TEST_TAG", "Info message")
        CoreLog.w("TEST_TAG", "Warn message")
        CoreLog.e("TEST_TAG", "Error message")
        CoreLog.wtf("TEST_TAG", "Assert message")
        CoreLog.json("TEST_TAG", "{\"key\":\"val\"}")
        CoreLog.xml("TEST_TAG", "<tag>val</tag>")

        assertEquals(8, fakeDelegate.logs.size)
        assertEquals(FakeLogDelegate.LogEntry("VERBOSE", "TEST_TAG", "Verbose message"), fakeDelegate.logs[0])
        assertEquals(FakeLogDelegate.LogEntry("DEBUG", "TEST_TAG", "Debug message"), fakeDelegate.logs[1])
        assertEquals(FakeLogDelegate.LogEntry("INFO", "TEST_TAG", "Info message"), fakeDelegate.logs[2])
        assertEquals(FakeLogDelegate.LogEntry("WARN", "TEST_TAG", "Warn message"), fakeDelegate.logs[3])
        assertEquals(FakeLogDelegate.LogEntry("ERROR", "TEST_TAG", "Error message"), fakeDelegate.logs[4])
        assertEquals(FakeLogDelegate.LogEntry("WTF", "TEST_TAG", "Assert message"), fakeDelegate.logs[5])
        assertEquals(FakeLogDelegate.LogEntry("JSON", "TEST_TAG", "{\"key\":\"val\"}"), fakeDelegate.logs[6])
        assertEquals(FakeLogDelegate.LogEntry("XML", "TEST_TAG", "<tag>val</tag>"), fakeDelegate.logs[7])
    }

    @Test
    fun testShowLogUiAndReadLog() {
        fakeDelegate.readLogResult = listOf("line1", "line2")
        var readLines = emptyList<String>()
        CoreLog.readLog { lines ->
            readLines = lines
        }
        assertEquals(listOf("line1", "line2"), readLines)

        CoreLog.showLogUi()
        assertEquals(1, fakeDelegate.showLogUiCalledCount)
    }
}
