package xxx.yyy.zzz.feature.discover.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import xxx.yyy.zzz.core.database.dao.VideoDao
import xxx.yyy.zzz.core.database.model.toDomain
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 发现探索模块仓储接口。
 */
interface DiscoverRepository {
    /**
     * 获取运营活动与热门 Banner 列表。
     */
    fun getBanners(): Flow<List<DiscoverBanner>>

    /**
     * 获取实时热搜榜单数据。
     */
    fun getTrendingTopics(): Flow<List<TrendingTopic>>

    /**
     * 获取探索瀑布流视频数据。
     */
    fun getExploreVideos(
        topic: String?,
        query: String?,
    ): Flow<List<VideoItem>>
}

/**
 * 发现探索模块仓储实现类。
 */
class DiscoverRepositoryImpl(
    private val videoDao: VideoDao,
) : DiscoverRepository {
    override fun getBanners(): Flow<List<DiscoverBanner>> =
        flow {
            emit(
                listOf(
                    DiscoverBanner(
                        id = "b_1",
                        title = "2026 影像季短视频大赛",
                        subtitle = "用镜头定格心动瞬间 · 瓜分百万创作流量",
                        tag = "热门活动",
                        coverUrl = "https://picsum.photos/800/400?random=banner1",
                    ),
                    DiscoverBanner(
                        id = "b_2",
                        title = "Android 架构师进阶计划",
                        subtitle = "探索 Compose 现代响应式短视频流开发",
                        tag = "技术专题",
                        coverUrl = "https://picsum.photos/800/400?random=banner2",
                    ),
                ),
            )
        }

    override fun getTrendingTopics(): Flow<List<TrendingTopic>> =
        flow {
            emit(
                listOf(
                    TrendingTopic(
                        rank = 1,
                        title = "Android 现代架构实战",
                        tag = "技术",
                        hotScore = "168.2w",
                        isHot = true,
                    ),
                    TrendingTopic(
                        rank = 2,
                        title = "VideoKit 短视频播放器发布",
                        tag = "新发布",
                        hotScore = "142.0w",
                        isNew = true,
                    ),
                    TrendingTopic(
                        rank = 3,
                        title = "夏日晚霞摄影挑战",
                        tag = "摄影日常",
                        hotScore = "98.5w",
                        isHot = true,
                    ),
                    TrendingTopic(
                        rank = 4,
                        title = "极简治愈系生活记录",
                        tag = "生活",
                        hotScore = "76.4w",
                    ),
                    TrendingTopic(
                        rank = 5,
                        title = "手冲咖啡风味地图",
                        tag = "美食",
                        hotScore = "52.1w",
                    ),
                ),
            )
        }

    override fun getExploreVideos(
        topic: String?,
        query: String?,
    ): Flow<List<VideoItem>> =
        videoDao.getVideosFlow().map { entities ->
            val list = entities.map { it.toDomain() }
            when {
                !query.isNullOrBlank() -> {
                    list.filter {
                        it.title.contains(query, ignoreCase = true) ||
                            it.author.nickname.contains(query, ignoreCase = true) ||
                            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    }
                }

                !topic.isNullOrBlank() && topic != "全部" -> {
                    list.filter {
                        it.tags.any { tag -> tag.contains(topic, ignoreCase = true) } ||
                            it.title.contains(topic, ignoreCase = true)
                    }
                }

                else -> {
                    list
                }
            }
        }
}
