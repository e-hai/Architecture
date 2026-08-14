package xxx.yyy.zzz.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import xxx.yyy.zzz.core.model.Author
import xxx.yyy.zzz.core.model.MusicTrack
import xxx.yyy.zzz.core.model.VideoItem

/**
 * 短视频本地缓存数据库实体。
 * 作为离线优先（Offline-First）架构的单一可信源（SSOT）。
 *
 * @property id 视频唯一 ID
 * @property title 视频标题
 * @property videoUrl 视频播放 URL
 * @property coverUrl 视频封面 URL
 * @property authorId 作者 ID
 * @property authorNickname 作者昵称
 * @property authorAvatarUrl 作者头像
 * @property authorIsFollowing 是否关注该作者
 * @property musicTitle 背景音乐标题
 * @property musicArtist 背景音乐作者
 * @property likeCount 点赞总数
 * @property commentCount 评论总数
 * @property shareCount 分享总数
 * @property bookmarkCount 收藏总数
 * @property isLiked 是否点赞
 * @property isBookmarked 是否收藏
 * @property durationMs 视频时长
 * @property width 视频宽
 * @property height 视频高
 * @property createdAtEpochMillis 创建时间戳毫秒
 */
@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val videoUrl: String,
    val coverUrl: String,
    val authorId: String,
    val authorNickname: String,
    val authorAvatarUrl: String,
    val authorIsFollowing: Boolean = false,
    val musicTitle: String = "",
    val musicArtist: String = "",
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,
    val shareCount: Long = 0L,
    val bookmarkCount: Long = 0L,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val createdAtEpochMillis: Long = 0L,
)

/**
 * 将 [VideoEntity] 实体转换为领域模型 [VideoItem]。
 */
fun VideoEntity.toDomain(): VideoItem =
    VideoItem(
        id = id,
        title = title,
        videoUrl = videoUrl,
        coverUrl = coverUrl,
        author =
            Author(
                id = authorId,
                nickname = authorNickname,
                avatarUrl = authorAvatarUrl,
                isFollowing = authorIsFollowing,
            ),
        music =
            if (musicTitle.isNotBlank()) {
                MusicTrack(
                    id = "music_$id",
                    title = musicTitle,
                    artist = musicArtist,
                )
            } else {
                null
            },
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        bookmarkCount = bookmarkCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        durationMs = durationMs,
        width = width,
        height = height,
        createdAt = if (createdAtEpochMillis > 0) Instant.fromEpochMilliseconds(createdAtEpochMillis) else null,
    )

/**
 * 将领域模型 [VideoItem] 转换为数据库实体 [VideoEntity]。
 */
fun VideoItem.toEntity(): VideoEntity =
    VideoEntity(
        id = id,
        title = title,
        videoUrl = videoUrl,
        coverUrl = coverUrl,
        authorId = author.id,
        authorNickname = author.nickname,
        authorAvatarUrl = author.avatarUrl,
        authorIsFollowing = author.isFollowing,
        musicTitle = music?.title.orEmpty(),
        musicArtist = music?.artist.orEmpty(),
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        bookmarkCount = bookmarkCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        durationMs = durationMs,
        width = width,
        height = height,
        createdAtEpochMillis = createdAt?.toEpochMilliseconds() ?: 0L,
    )
