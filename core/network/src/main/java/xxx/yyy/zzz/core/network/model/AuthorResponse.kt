package xxx.yyy.zzz.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xxx.yyy.zzz.core.model.Author

/**
 * 创作者网络响应 DTO。
 */
@Serializable
data class AuthorResponse(
    @SerialName("id")
    val id: String,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("bio")
    val bio: String = "",
    @SerialName("is_following")
    val isFollowing: Boolean = false,
    @SerialName("follower_count")
    val followerCount: Long = 0L,
    @SerialName("following_count")
    val followingCount: Long = 0L,
    @SerialName("like_count")
    val likeCount: Long = 0L,
)

/**
 * 将 [AuthorResponse] DTO 转换为领域模型 [Author]。
 */
fun AuthorResponse.toDomain(): Author =
    Author(
        id = id,
        nickname = nickname,
        avatarUrl = avatarUrl,
        bio = bio,
        isFollowing = isFollowing,
        followerCount = followerCount,
        followingCount = followingCount,
        likeCount = likeCount,
    )
