package com.shortvideo.app.feature.feed.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 首页短视频流导航公钥 (NavKey)。
 *
 * @property initialVideoId 初始定位播放的视频 ID，为空时从头播放推荐流
 */
@Serializable
data class FeedNavKey(
    val initialVideoId: String? = null,
) : NavKey
