package xxx.yyy.zzz.feature.feed.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import xxx.yyy.zzz.core.database.dao.VideoDao
import xxx.yyy.zzz.core.database.model.toDomain
import xxx.yyy.zzz.core.database.model.toEntity
import xxx.yyy.zzz.core.model.Author
import xxx.yyy.zzz.core.model.MusicTrack
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 短视频 Feed 仓储接口。
 */
interface FeedRepository {
    /**
     * 响应式观察本地缓存的短视频流 (SSOT 离线优先)。
     */
    fun getFeedVideos(): Flow<List<VideoItem>>

    /**
     * 刷新短视频数据。
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
        // 演示种子数据（真实视频流可替换为生产 CDN 视频 URL）
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
                title = "大自然的美好瞬间 🌿 感受山川与晨雾的宁静 #旅行 #治愈系 #风景",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=101",
                author =
                    Author(
                        id = "author_1",
                        nickname = "@山野行者",
                        avatarUrl = "https://picsum.photos/120/120?random=1",
                        isFollowing = false,
                        followerCount = 45200L,
                    ),
                music =
                    MusicTrack(
                        id = "m_1",
                        title = "晨曦微光 - 原声音乐",
                        artist = "神秘音乐家",
                    ),
                tags = listOf("旅行", "治愈系", "风景"),
                likeCount = 8920L,
                commentCount = 342L,
                shareCount = 618L,
                bookmarkCount = 1204L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_102",
                title = "极简主义生活日常 ☕ 一杯手冲咖啡开启美好周末 #生活美学 #日常 #咖啡",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=102",
                author =
                    Author(
                        id = "author_2",
                        nickname = "@慢调生活馆",
                        avatarUrl = "https://picsum.photos/120/120?random=2",
                        isFollowing = true,
                        followerCount = 128000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_2",
                        title = "Sunday Morning Coffee",
                        artist = "Lofi City",
                    ),
                tags = listOf("生活美学", "日常", "咖啡"),
                likeCount = 15300L,
                commentCount = 890L,
                shareCount = 2300L,
                bookmarkCount = 4500L,
                isLiked = true,
                isBookmarked = true,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            VideoItem(
                id = "v_103",
                title = "Compose Multiplatform + VideoKit 极致音视频体验！🚀 丝滑到停不下来 #Android开发 #Kotlin",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                coverUrl = "https://picsum.photos/600/1000?random=103",
                author =
                    Author(
                        id = "author_3",
                        nickname = "@Android架构师",
                        avatarUrl = "https://picsum.photos/120/120?random=3",
                        isFollowing = false,
                        followerCount = 98000L,
                    ),
                music =
                    MusicTrack(
                        id = "m_3",
                        title = "Future Cyber Tech",
                        artist = "Kotlin Beat",
                    ),
                tags = listOf("Android开发", "Kotlin", "开源"),
                likeCount = 67200L,
                commentCount = 1420L,
                shareCount = 8900L,
                bookmarkCount = 12300L,
                isLiked = false,
                isBookmarked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
        )
}
