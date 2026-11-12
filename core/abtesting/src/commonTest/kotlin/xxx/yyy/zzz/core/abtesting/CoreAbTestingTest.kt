package xxx.yyy.zzz.core.abtesting

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * CoreAbTesting 门面契约单元测试。
 */
class CoreAbTestingTest {
    private val fakeDelegate = FakeAbTestingDelegate()

    @BeforeTest
    fun setUp() {
        CoreAbTesting.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CoreAbTesting.install(DefaultAbTestingDelegate)
    }

    @Test
    fun testDispatchesToDelegate() {
        CoreAbTesting.doInit()
        fakeDelegate.values["flag_bool"] = true
        fakeDelegate.values[AbTestingKeys.WELCOME_MESSAGE] = "Custom Welcome"
        fakeDelegate.values["limit_long"] = 42L
        fakeDelegate.values["ratio_double"] = 3.14

        assertEquals(1, fakeDelegate.initCount)
        assertTrue(CoreAbTesting.getBoolean("flag_bool", false))
        assertEquals("Custom Welcome", CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, "Fallback"))
        assertEquals(42L, CoreAbTesting.getLong("limit_long", 0L))
        assertEquals(3.14, CoreAbTesting.getDouble("ratio_double", 0.0))
        assertEquals("Fallback", CoreAbTesting.getString("missing_key", "Fallback"))
    }
}
