package xxx.yyy.zzz.core.video

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * CoreVideo 跨平台契约单元测试。
 * 遵循 Fake over Mock 铁律，通过强类型 FakeVideoDelegate 验证门面调用。
 */
class CoreVideoTest {
    private lateinit var fakeDelegate: FakeVideoDelegate

    @BeforeTest
    fun setUp() {
        fakeDelegate = FakeVideoDelegate()
        CoreVideo.install(fakeDelegate)
    }

    @Test
    fun testDoInit() {
        assertFalse(fakeDelegate.isInitialized)
        CoreVideo.doInit()
        assertTrue(fakeDelegate.isInitialized)
    }

    @Test
    fun testPreloadAndCache() {
        val testUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        assertFalse(CoreVideo.isCached(testUrl))
        assertEquals(0L, CoreVideo.getCachedBytes(testUrl))

        CoreVideo.preload(testUrl, bytes = 1024 * 1024)
        assertTrue(CoreVideo.isCached(testUrl))
        assertEquals(1024 * 1024L, CoreVideo.getCachedBytes(testUrl))
    }

    @Test
    fun testPreloadBidirectional() {
        val prev = "https://example.com/video1.mp4"
        val next = "https://example.com/video2.mp4"

        CoreVideo.preloadBidirectional(prevUrl = prev, nextUrl = next, bytes = 512 * 1024)

        assertEquals(listOf(next, prev), fakeDelegate.preloadedUrls)
        assertTrue(CoreVideo.isCached(prev))
        assertTrue(CoreVideo.isCached(next))
    }

    @Test
    fun testSaveAndGetProgress() {
        val videoId = "vid_1001"
        assertEquals(0L, CoreVideo.getProgress(videoId))

        CoreVideo.saveProgress(videoId, 45000L, 120000L)
        assertEquals(45000L, CoreVideo.getProgress(videoId))

        CoreVideo.clearProgress(videoId)
        assertEquals(0L, CoreVideo.getProgress(videoId))
    }

    @Test
    fun testClearCache() {
        val testUrl = "https://example.com/video.mp4"
        CoreVideo.preload(testUrl, 2048)
        assertTrue(CoreVideo.isCached(testUrl))

        CoreVideo.clearCache()
        assertFalse(CoreVideo.isCached(testUrl))
        assertTrue(fakeDelegate.clearCacheCalled)
    }
}
