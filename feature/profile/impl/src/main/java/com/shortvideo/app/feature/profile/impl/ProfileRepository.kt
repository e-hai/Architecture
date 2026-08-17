package com.shortvideo.app.feature.profile.impl

import com.shortvideo.app.core.database.dao.VideoDao
import com.shortvideo.app.core.database.model.toDomain
import com.shortvideo.app.core.model.Author
import com.shortvideo.app.core.model.VideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * 个人中心/用户主页仓储接口。
 */
interface ProfileRepository {
    /**
     * 获取指定用户的个人资料。
     */
    fun getUserProfile(userId: String?): Flow<Author>

    /**
     * 获取用户发布的作品列表。
     */
    fun getUserWorks(userId: String?): Flow<List<VideoItem>>

    /**
     * 获取用户点赞的视频列表。
     */
    fun getLikedVideos(): Flow<List<VideoItem>>

    /**
     * 获取用户收藏的视频列表。
     */
    fun getBookmarkedVideos(): Flow<List<VideoItem>>
}

/**
 * 个人中心仓储实现类。
 */
class ProfileRepositoryImpl(
    private val videoDao: VideoDao,
) : ProfileRepository {
    override fun getUserProfile(userId: String?): Flow<Author> =
        flow {
            emit(
                Author(
                    id = userId ?: "self",
                    nickname = if (userId == null || userId == "self") "Antigravity 创作者" else "@创作者_$userId",
                    avatarUrl = "https://picsum.photos/200/200?random=${userId?.hashCode() ?: 999}",
                    bio = "记录生活中的每一个闪光瞬间 ✨ | 摄影 · 旅行 · 极简美学",
                    isFollowing = false,
                    followerCount = 42800L,
                ),
            )
        }

    override fun getUserWorks(userId: String?): Flow<List<VideoItem>> =
        videoDao.getVideosFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getLikedVideos(): Flow<List<VideoItem>> =
        videoDao.getVideosFlow().map { entities ->
            entities.filter { it.isLiked }.map { it.toDomain() }
        }

    override fun getBookmarkedVideos(): Flow<List<VideoItem>> =
        videoDao.getVideosFlow().map { entities ->
            entities.filter { it.isBookmarked }.map { it.toDomain() }
        }
}
