package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import com.kit.video.cache.VideoCacheManager
import com.kit.video.vod.VodProgressStore
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.video.VideoDelegate

/**
 * Android 视频播放与缓存委托实现（1:1 透传 VideoKit-Android 原生 SDK）。
 */
internal class AndroidVideoDelegate(
    private val context: Context,
) : VideoDelegate {
    override fun doInit() {
        VodProgressStore.init(context)
        CoreLog.i(LogTags.VIDEO, "Android VideoKit (VodProgressStore) initialized.")
    }

    override fun preload(
        url: String,
        bytes: Long,
    ) {
        VideoCacheManager.preload(context, url, bytes)
    }

    override fun preloadBidirectional(
        prevUrl: String?,
        nextUrl: String?,
        bytes: Long,
    ) {
        VideoCacheManager.preloadBidirectional(context, prevUrl, nextUrl, bytes)
    }

    override fun getCachedBytes(url: String): Long = VideoCacheManager.getCachedBytes(context, url)

    override fun isCached(
        url: String,
        minBytes: Long,
    ): Boolean = VideoCacheManager.isCached(context, url, minBytes)

    override fun clearCache() {
        VideoCacheManager.clearCache(context)
        CoreLog.i(LogTags.VIDEO, "Android VideoKit cache cleared.")
    }

    override fun saveProgress(
        videoId: String,
        positionMs: Long,
        durationMs: Long,
    ) {
        val duration = if (durationMs > 0) durationMs else (positionMs * 2).coerceAtLeast(10000L)
        VodProgressStore.saveProgress(videoId, positionMs, duration)
    }

    override fun getProgress(videoId: String): Long = VodProgressStore.getProgress(videoId)

    override fun clearProgress(videoId: String) {
        VodProgressStore.clearProgress(videoId)
        CoreLog.i(LogTags.VIDEO, "Android VodProgressStore cleared for: $videoId.")
    }
}
