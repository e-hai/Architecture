package xxx.yyy.zzz.feature.discover.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 发现/探索页面导航公钥 (NavKey)。
 *
 * @property initialTopic 初始筛选的热门话题标签（如 "生活", "摄影"），为空时展示全量推荐
 */
@Serializable
data class DiscoverNavKey(
    val initialTopic: String? = null,
) : NavKey
