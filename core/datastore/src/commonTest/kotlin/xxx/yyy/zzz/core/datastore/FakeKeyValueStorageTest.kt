package xxx.yyy.zzz.core.datastore

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 契约驱动单元测试示例（Fake over Mock）。
 */
class FakeKeyValueStorageTest {
    @Test
    fun testPutAndGetString() =
        runTest {
            val storage = FakeKeyValueStorage()
            assertNull(storage.getString("test_key"))

            storage.putString("test_key", "hello_kmp")
            assertEquals("hello_kmp", storage.getString("test_key"))

            storage.remove("test_key")
            assertNull(storage.getString("test_key"))
        }

    @Test
    fun testStringFlowEmissions() =
        runTest {
            val storage = FakeKeyValueStorage()
            storage.stringFlow("flow_key").test {
                assertEquals(null, awaitItem())

                storage.putString("flow_key", "first_value")
                assertEquals("first_value", awaitItem())

                storage.putString("flow_key", "second_value")
                assertEquals("second_value", awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
