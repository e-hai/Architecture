package xxx.yyy.zzz.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 视频评论项领域模型。
 *
 * @property id 评论唯一 ID
 * @property videoId 所属视频 ID
 * @property user 评论发布者信息
 * @property content 评论文本内容
 * @property likeCount 获赞数量
 * @property isLiked 当前登录用户是否已点赞该评论
 * @property createdAt 发布时间
 * @property replyCount 二级回复数量
 * @property replyList 二级回复预览列表
 */
@Serializable
data class CommentItem(
    val id: String,
    val videoId: String,
    val user: Author,
    val content: String,
    val likeCount: Long = 0L,
    val isLiked: Boolean = false,
    val createdAt: Instant? = null,
    val replyCount: Int = 0,
    val replyList: List<CommentItem> = emptyList(),
)
