package xxx.yyy.zzz.core.push

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * CorePush 门面契约单元测试。
 */
class CorePushTest {
    private val fakeDelegate = FakePushDelegate()

    @BeforeTest
    fun setUp() {
        CorePush.install(fakeDelegate)
    }

    @AfterTest
    fun tearDown() {
        fakeDelegate.clear()
        CorePush.install(DefaultPushDelegate)
    }

    @Test
    fun testPushDispatchesToDelegate() {
        CorePush.doInit(registerFcm = true)
        CorePush.registerPush()
        CorePush.setPushToken("test_token_888")

        assertEquals(true, fakeDelegate.initializedWithFcm)
        assertTrue(fakeDelegate.registered)
        assertEquals("test_token_888", fakeDelegate.currentToken)
    }
}
