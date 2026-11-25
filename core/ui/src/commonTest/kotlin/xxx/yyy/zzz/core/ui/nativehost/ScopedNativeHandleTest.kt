package xxx.yyy.zzz.core.ui.nativehost

import kotlinx.collections.immutable.persistentMapOf
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

private class FakeScopedSession(
    val sessionId: String,
) : ScopedNativeHandle {
    var releaseCount: Int = 0
        private set

    override val isClosed: Boolean
        get() = releaseCount > 0

    override fun close() {
        releaseCount += 1
    }
}

/**
 * 局部原生对象句柄、工厂与跨平台视图注册表契约测试（Fake over Mock）。
 */
class ScopedNativeHandleTest {
    @AfterTest
    fun tearDown() {
        NativeViewRegistry.clear()
    }

    @Test
    fun testFactoryCreatesIsolatedMultiInstanceHandles() {
        var counter = 0
        val factory =
            ScopedNativeFactory<String, FakeScopedSession> { channel ->
                counter += 1
                FakeScopedSession(sessionId = "$channel-$counter")
            }

        val first = factory.create("player")
        val second = factory.create("player")

        assertNotSame(first, second)
        assertEquals("player-1", first.sessionId)
        assertEquals("player-2", second.sessionId)

        first.close()
        assertTrue(first.isClosed)
        assertFalse(second.isClosed)
    }

    @Test
    fun testIdempotentFakeHandleCloseSemantics() {
        val idempotentHandle =
            object : ScopedNativeHandle {
                var closeInvocations: Int = 0
                    private set
                override var isClosed: Boolean = false
                    private set

                override fun close() {
                    if (isClosed) return
                    isClosed = true
                    closeInvocations += 1
                }
            }

        assertFalse(idempotentHandle.isClosed)
        idempotentHandle.close()
        idempotentHandle.close()
        assertTrue(idempotentHandle.isClosed)
        assertEquals(1, idempotentHandle.closeInvocations)
    }

    @Test
    fun testNativeViewRequestDefaultsAndParams() {
        val defaultRequest = NativeViewRequest(viewType = "native_player")
        assertEquals("native_player", defaultRequest.viewType)
        assertEquals("", defaultRequest.instanceKey)
        assertTrue(defaultRequest.params.isEmpty())

        val parameterized =
            NativeViewRequest(
                viewType = "native_player",
                instanceKey = "feed-item-42",
                params = persistentMapOf("url" to "https://example.com/a.mp4", "autoplay" to "true"),
            )
        assertEquals("feed-item-42", parameterized.instanceKey)
        assertEquals("https://example.com/a.mp4", parameterized.params["url"])
        assertEquals("true", parameterized.params["autoplay"])
    }

    @Test
    fun testNativeViewRegistryEmptyOperationsAreSafe() {
        assertFalse(NativeViewRegistry.isRegistered("unregistered_view"))
        NativeViewRegistry.unregister("unregistered_view")
        NativeViewRegistry.clear()
        assertFalse(NativeViewRegistry.isRegistered("unregistered_view"))
    }
}
