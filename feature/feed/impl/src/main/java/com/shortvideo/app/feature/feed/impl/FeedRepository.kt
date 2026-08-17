package com.shortvideo.app.feature.feed.impl

import com.shortvideo.app.core.database.dao.VideoDao
import com.shortvideo.app.core.database.model.toDomain
import com.shortvideo.app.core.database.model.toEntity
import com.shortvideo.app.core.model.Author
import com.shortvideo.app.core.model.MusicTrack
import com.shortvideo.app.core.model.VideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * 短视频 Feed 仓储接口。
 */
interface FeedRepository {
    /**
     * 响应式观察本地缓存的推荐短视频流 (SSOT 离线优先)。
     */
    fun getFeedVideos(): Flow<List<VideoItem>>

    /**
     * 刷新短视频数据并同步真实播放源。
     */
    suspend fun refreshFeed()

    /**
     * 切换指定视频的点赞状态。
     */
    suspend fun toggleLike(videoId: String)

    /**
     * 切换指定视频的收藏状态。
     */
    suspend fun toggleBookmark(videoId: String)
}

/**
 * 短视频 Feed 仓储实现类。
 *
 * @param videoDao Room 本地视频数据访问对象
 */
class FeedRepositoryImpl(
    private val videoDao: VideoDao,
) : FeedRepository {
    override fun getFeedVideos(): Flow<List<VideoItem>> =
        videoDao.getVideosFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshFeed() {
        // 全量同步真实稳定、高可用 CDN 播放源
        val sampleVideos = createSampleVideos()
        videoDao.insertVideos(sampleVideos.map { it.toEntity() })
    }

    override suspend fun toggleLike(videoId: String) {
        val video = videoDao.getVideoById(videoId) ?: return
        val newLiked = !video.isLiked
        val newCount = if (newLiked) video.likeCount + 1 else (video.likeCount - 1).coerceAtLeast(0)
        videoDao.updateLikeState(videoId, newLiked, newCount)
    }

    override suspend fun toggleBookmark(videoId: String) {
        val video = videoDao.getVideoById(videoId) ?: return
        val newBookmarked = !video.isBookmarked
        val newCount = if (newBookmarked) video.bookmarkCount + 1 else (video.bookmarkCount - 1).coerceAtLeast(0)
        videoDao.updateBookmarkState(videoId, newBookmarked, newCount)
    }

    private fun createSampleVideos(): List<VideoItem> =
        listOf(
            VideoItem(
                id = "v_101",
                title = "潜入深蓝秘境 🌊 见证海洋与生命的壮阔奇迹 #自然 #海洋 #探索",
                videoUrl = "https://vjs.zencdn.net/v/oceans.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=101",
                author =
                    Author(
                        id = "author_1",
                        nickname = "@深海摄影师_Leo",
                        avatarUrl = "https://picsum.photos/120/120?random=1",
                        isFollowing = false,
                        followerCount = 186000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_1",
                        title = "Ocean Symphony - 原创深海旋律",
                        artist = "Blue Planet Sound",
                    ),
                tags = listOf("自然", "海洋", "探索", "摄影日常"),
                likeCount = 89200L,
                commentCount = 3420L,
                shareCount = 6180L,
                bookmarkCount = 12040L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_102",
                title = "极限冲浪与金色夕阳 🏄 追逐风浪中的终极自由 #运动 #冲浪 #旅行",
                videoUrl = "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-720p.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=102",
                author =
                    Author(
                        id = "author_2",
                        nickname = "@极限浪人_John",
                        avatarUrl = "https://picsum.photos/120/120?random=2",
                        isFollowing = true,
                        followerCount = 342000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_2",
                        title = "Golden Wave Energy",
                        artist = "Surf Beats",
                    ),
                tags = listOf("运动", "冲浪", "旅行", "治愈系风景"),
                likeCount = 153000L,
                commentCount = 8920L,
                shareCount = 23000L,
                bookmarkCount = 45000L,
                isLiked = true,
                isBookmarked = true,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_103",
                title = "梦幻水母深海发光 🪼 极简治愈深海光影秀 #治愈 #微观世界 #极简美学",
                videoUrl = "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_1MB.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=103",
                author =
                    Author(
                        id = "author_3",
                        nickname = "@光影微观社",
                        avatarUrl = "https://picsum.photos/120/120?random=3",
                        isFollowing = false,
                        followerCount = 98000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_3",
                        title = "Ambient Luminous Drift",
                        artist = "Zen Atmosphere",
                    ),
                tags = listOf("治愈", "微观世界", "极简生活"),
                likeCount = 67200L,
                commentCount = 1420L,
                shareCount = 8900L,
                bookmarkCount = 12300L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_104",
                title = "繁花绽放的延时奇迹 🌸 一秒看尽春暖花开 #摄影 #延时摄影 #花开",
                videoUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=104",
                author =
                    Author(
                        id = "author_4",
                        nickname = "@自然延时记录",
                        avatarUrl = "https://picsum.photos/120/120?random=4",
                        isFollowing = false,
                        followerCount = 76000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_4",
                        title = "Spring Waltz - 钢琴慢摇",
                        artist = "Classical Ambient",
                    ),
                tags = listOf("摄影", "延时摄影", "摄影日常"),
                likeCount = 42100L,
                commentCount = 890L,
                shareCount = 3100L,
                bookmarkCount = 9800L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_105",
                title = "3D 奇幻动画短片精选 · 寻龙少女与羁绊之箭 🏹 #动画 #3D #艺术",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=105",
                author =
                    Author(
                        id = "author_5",
                        nickname = "@CG动画视界",
                        avatarUrl = "https://picsum.photos/120/120?random=5",
                        isFollowing = true,
                        followerCount = 520000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_5",
                        title = "Epic Quest Soundtrack",
                        artist = "Blender Studio",
                    ),
                tags = listOf("动画", "3D", "艺术", "Kotlin/Compose"),
                likeCount = 129000L,
                commentCount = 4510L,
                shareCount = 18200L,
                bookmarkCount = 36700L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_106",
                title = "森林小萌物大冒险 🐰 森林深处的大耳朵伙伴 #治愈 #萌宠 #动漫",
                videoUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=106",
                author =
                    Author(
                        id = "author_6",
                        nickname = "@森林治愈系",
                        avatarUrl = "https://picsum.photos/120/120?random=6",
                        isFollowing = false,
                        followerCount = 210000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_6",
                        title = "Forest Happy Sunshine",
                        artist = "Joyful Acoustic",
                    ),
                tags = listOf("治愈", "萌宠", "手冲咖啡"),
                likeCount = 95400L,
                commentCount = 2800L,
                shareCount = 12000L,
                bookmarkCount = 21000L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
        )
}
