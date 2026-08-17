package com.shortvideo.app.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 短视频核心领域模型。
 *
 * @property id 视频唯一标识 ID
 * @property title 视频标题与文案
 * @property videoUrl 视频流播放地址
 * @property coverUrl 视频封面图地址
 * @property author 视频作者信息
 * @property music 视频配乐信息
 * @property tags 关联话题/标签列表（如 #生活, #美食）
 * @property likeCount 点赞总数
 * @property commentCount 评论总数
 * @property shareCount 分享总数
 * @property bookmarkCount 收藏总数
 * @property isLiked 当前登录用户是否已点赞
 * @property isBookmarked 当前登录用户是否已收藏
 * @property durationMs 视频时长（毫秒）
 * @property width 视频分辨率宽度
 * @property height 视频分辨率高度
 * @property createdAt 发布时间戳
 */
@Serializable
data class VideoItem(
    val id: String,
    val title: String,
    val videoUrl: String,
    val coverUrl: String,
    val author: Author,
    val music: MusicTrack? = null,
    val tags: List<String> = emptyList(),
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,
    val shareCount: Long = 0L,
    val bookmarkCount: Long = 0L,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Instant? = null,
)
