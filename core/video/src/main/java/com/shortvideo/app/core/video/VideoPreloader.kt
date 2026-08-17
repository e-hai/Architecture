package com.shortvideo.app.core.video

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.an.video.exoplayer.MediaCacheFactory
import com.kit.log.LogKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 视频后台智能预加载器。
 * 用于在用户滑动浏览时提前预热下一条/前一条视频的前 2MB 数据，达成 0ms 秒开无黑屏体验。
 */
@OptIn(UnstableApi::class)
object VideoPreloader {
    private const val TAG = "VideoPreloader"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private const val PRELOAD_BYTES = 2L * 1024L * 1024L // 预加载前 2MB

    /**
     * 预加载指定列表的视频。
     *
     * @param context Context
     * @param urls 待预加载的视频 URL 列表
     */
    fun preload(
        context: Context,
        urls: List<String>,
    ) {
        val appContext = context.applicationContext
        urls.forEach { url ->
            if (url.isBlank() || !url.startsWith("http")) return@forEach
            scope.launch {
                try {
                    val dataSource = MediaCacheFactory.getCacheFactory(appContext).createDataSource()
                    if (dataSource is CacheDataSource) {
                        val dataSpec =
                            DataSpec
                                .Builder()
                                .setUri(Uri.parse(url))
                                .setLength(PRELOAD_BYTES)
                                .build()

                        val cacheWriter =
                            CacheWriter(
                                dataSource,
                                dataSpec,
                                null,
                                null,
                            )
                        cacheWriter.cache()
                        LogKit.d(TAG, "Preloaded video chunk (2MB) for: $url")
                    }
                } catch (e: Exception) {
                    LogKit.w(TAG, "Preload skipped or error: ${e.message}")
                }
            }
        }
    }
}
