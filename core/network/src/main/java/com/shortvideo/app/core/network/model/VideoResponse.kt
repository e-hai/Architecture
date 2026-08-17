package com.shortvideo.app.core.network.model

import com.shortvideo.app.core.model.VideoItem
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 短视频网络响应 DTO。
 */
@Serializable
data class VideoResponse(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("video_url")
    val videoUrl: String,
    @SerialName("cover_url")
    val coverUrl: String,
    @SerialName("author")
    val author: AuthorResponse,
    @SerialName("music")
    val music: MusicTrackResponse? = null,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    @SerialName("like_count")
    val likeCount: Long = 0L,
    @SerialName("comment_count")
    val commentCount: Long = 0L,
    @SerialName("share_count")
    val shareCount: Long = 0L,
    @SerialName("bookmark_count")
    val bookmarkCount: Long = 0L,
    @SerialName("is_liked")
    val isLiked: Boolean = false,
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean = false,
    @SerialName("duration_ms")
    val durationMs: Long = 0L,
    @SerialName("width")
    val width: Int = 0,
    @SerialName("height")
    val height: Int = 0,
    @SerialName("created_at_epoch_ms")
    val createdAtEpochMs: Long = 0L,
)

/**
 * 将 [VideoResponse] DTO 转换为领域模型 [VideoItem]。
 */
fun VideoResponse.toDomain(): VideoItem =
    VideoItem(
        id = id,
        title = title,
        videoUrl = videoUrl,
        coverUrl = coverUrl,
        author = author.toDomain(),
        music = music?.toDomain(),
        tags = tags,
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        bookmarkCount = bookmarkCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        durationMs = durationMs,
        width = width,
        height = height,
        createdAt = if (createdAtEpochMs > 0) Instant.fromEpochMilliseconds(createdAtEpochMs) else null,
    )
