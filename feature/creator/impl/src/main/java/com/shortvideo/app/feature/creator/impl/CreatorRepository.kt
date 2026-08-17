package com.shortvideo.app.feature.creator.impl

import com.shortvideo.app.core.database.dao.VideoDao
import com.shortvideo.app.core.database.model.toEntity
import com.shortvideo.app.core.model.Author
import com.shortvideo.app.core.model.MusicTrack
import com.shortvideo.app.core.model.VideoItem
import kotlinx.datetime.Instant

/**
 * 创作模块数据仓储接口。
 */
interface CreatorRepository {
    /**
     * 发布并持久化新拍摄的短视频作品。
     */
    suspend fun publishVideo(
        title: String,
        tags: List<String>,
        isPublic: Boolean,
    ): VideoItem
}

/**
 * 创作模块数据仓储实现类。
 */
class CreatorRepositoryImpl(
    private val videoDao: VideoDao,
) : CreatorRepository {
    override suspend fun publishVideo(
        title: String,
        tags: List<String>,
        isPublic: Boolean,
    ): VideoItem {
        val randomCoverIndex = (100..120).random()
        val newVideo =
            VideoItem(
                id = "v_created_${System.currentTimeMillis()}",
                title = title.ifBlank { "今天记录的美好瞬间 ✨ #生活 #日常" },
                videoUrl = "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-720p.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=$randomCoverIndex",
                author =
                    Author(
                        id = "self",
                        nickname = "Antigravity 创作者",
                        avatarUrl = "https://picsum.photos/200/200?random=999",
                        isFollowing = false,
                        followerCount = 42800L,
                    ),
                music =
                    MusicTrack(
                        id = "m_created",
                        title = "原创原声 - Antigravity",
                        artist = "Antigravity 创作者",
                    ),
                tags = tags,
                likeCount = 1L,
                commentCount = 0L,
                shareCount = 0L,
                bookmarkCount = 0L,
                isLiked = true,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            )

        // 插入本地 Room 数据库（离线优先 SSOT）
        videoDao.insertVideos(listOf(newVideo.toEntity()))
        return newVideo
    }
}
