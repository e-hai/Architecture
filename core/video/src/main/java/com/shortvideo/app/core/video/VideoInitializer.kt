package com.shortvideo.app.core.video

import android.content.Context

/**
 * VideoKit 初始化入口。
 * 集中配置视频缓存大小、预加载队列策略与软解配置，避免在 Application 中散落 SDK 细节。
 */
object VideoInitializer {
    /**
     * 初始化 VideoKit 基础设施。须在应用启动时（MyApplication.onCreate）调用一次。
     *
     * @param context Application Context
     * @param debug 是否为调试模式
     * @param maxCacheBytes 视频磁盘缓存大小，默认 1GB (1024L * 1024L * 1024L)
     */
    fun initialize(
        context: Context,
        debug: Boolean = false,
        maxCacheBytes: Long = 1024L * 1024L * 1024L,
    ) {
        // VideoKit 核心初始化逻辑（若 SDK 提供全局配置，在此统一装配）
    }
}
