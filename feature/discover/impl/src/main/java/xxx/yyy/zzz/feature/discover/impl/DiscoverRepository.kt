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
     * 获取热门话题列表。
     */
    fun getTrendingTopics(): Flow<List<TopicItem>>

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
    override fun getTrendingTopics(): Flow<List<TopicItem>> =
        flow {
            emit(
                listOf(
                    TopicItem(id = "t_all", title = "全部", heatCount = "推荐"),
                    TopicItem(id = "t_1", title = "摄影日常", heatCount = "128.5w 在看"),
                    TopicItem(id = "t_2", title = "治愈系风景", heatCount = "98.2w 在看"),
                    TopicItem(id = "t_3", title = "极简生活", heatCount = "76.4w 在看"),
                    TopicItem(id = "t_4", title = "手冲咖啡", heatCount = "52.1w 在看"),
                    TopicItem(id = "t_5", title = "Kotlin/Compose", heatCount = "45.0w 在看"),
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
                    list.filter { it.title.contains(query, ignoreCase = true) || it.author.nickname.contains(query, ignoreCase = true) }
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
