package xxx.yyy.zzz.core.video

/**
 * 契约驱动测试与本地开发专用的 Fake 视频委托实现。
 * 纯内存存储，支持对预加载、缓存状态与断点续播的断言验证。
 */
class FakeVideoDelegate : VideoDelegate {
    var isInitialized: Boolean = false
        private set

    val preloadedUrls = mutableListOf<String>()
    val cachedBytesMap = mutableMapOf<String, Long>()
    val progressStore = mutableMapOf<String, Long>()
    var clearCacheCalled: Boolean = false
        private set

    override fun doInit() {
        isInitialized = true
    }

    override fun preload(
        url: String,
        bytes: Long,
    ) {
        preloadedUrls.add(url)
        cachedBytesMap[url] = bytes
    }

    override fun preloadBidirectional(
        prevUrl: String?,
        nextUrl: String?,
        bytes: Long,
    ) {
        nextUrl?.let { preload(it, bytes) }
        prevUrl?.let { preload(it, bytes) }
    }

    override fun getCachedBytes(url: String): Long = cachedBytesMap[url] ?: 0L

    override fun isCached(
        url: String,
        minBytes: Long,
    ): Boolean = (cachedBytesMap[url] ?: 0L) >= minBytes

    override fun clearCache() {
        cachedBytesMap.clear()
        preloadedUrls.clear()
        clearCacheCalled = true
    }

    override fun saveProgress(
        videoId: String,
        positionMs: Long,
        durationMs: Long,
    ) {
        progressStore[videoId] = positionMs
    }

    override fun getProgress(videoId: String): Long = progressStore[videoId] ?: 0L

    override fun clearProgress(videoId: String) {
        progressStore.remove(videoId)
    }
}
