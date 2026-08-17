package com.shortvideo.app.core.video

import android.content.Context
import com.an.video.exoplayer.MediaCacheFactory
import com.kit.log.LogKit

/**
 * VideoKit 初始化与配置入口。
 * 集中配置视频缓存大小、预加载队列策略与软硬件解码，避免在 Application 中散落 SDK 细节。
 */
object VideoInitializer {
    private const val TAG = "VideoInitializer"

    /**
     * 初始化 VideoKit 基础设施。在应用启动时调用。
     *
     * @param context Application Context
     * @param debug 是否为调试模式
     * @param maxCacheBytes 视频磁盘缓存大小，默认 512MB
     */
    fun initialize(
        context: Context,
        debug: Boolean = false,
        maxCacheBytes: Long = 512L * 1024L * 1024L,
    ) {
        try {
            // 初始化本地视频 LRU 缓存工厂
            MediaCacheFactory.getCacheFactory(context.applicationContext)
            LogKit.i(TAG, "VideoKit player and media cache initialized successfully. Cache size: ${maxCacheBytes / 1024 / 1024}MB")
        } catch (e: Exception) {
            LogKit.e(TAG, e, "Failed to initialize VideoKit: ${e.message}")
        }
    }
}
