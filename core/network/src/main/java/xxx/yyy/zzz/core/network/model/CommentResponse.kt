package xxx.yyy.zzz.core.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xxx.yyy.zzz.core.model.CommentItem

/**
 * 评论网络响应 DTO。
 */
@Serializable
data class CommentResponse(
    @SerialName("id")
    val id: String,
    @SerialName("video_id")
    val videoId: String,
    @SerialName("user")
    val user: AuthorResponse,
    @SerialName("content")
    val content: String,
    @SerialName("like_count")
    val likeCount: Long = 0L,
    @SerialName("is_liked")
    val isLiked: Boolean = false,
    @SerialName("created_at_epoch_ms")
    val createdAtEpochMs: Long = 0L,
    @SerialName("reply_count")
    val replyCount: Int = 0,
    @SerialName("reply_list")
    val replyList: List<CommentResponse> = emptyList(),
)

/**
 * 将 [CommentResponse] DTO 转换为领域模型 [CommentItem]。
 */
fun CommentResponse.toDomain(): CommentItem =
    CommentItem(
        id = id,
        videoId = videoId,
        user = user.toDomain(),
        content = content,
        likeCount = likeCount,
        isLiked = isLiked,
        createdAt = if (createdAtEpochMs > 0) Instant.fromEpochMilliseconds(createdAtEpochMs) else null,
        replyCount = replyCount,
        replyList = replyList.map { it.toDomain() },
    )
