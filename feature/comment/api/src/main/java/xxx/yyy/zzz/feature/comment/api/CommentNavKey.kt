package xxx.yyy.zzz.feature.comment.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 评论模块导航公钥 (NavKey)。
 *
 * @property videoId 需要查看或发表评论的目标视频 ID
 */
@Serializable
data class CommentNavKey(
    val videoId: String,
) : NavKey
