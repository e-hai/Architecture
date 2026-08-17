package com.shortvideo.app.feature.comment.impl

import com.shortvideo.app.core.model.Author
import com.shortvideo.app.core.model.CommentItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

/**
 * 评论数据仓储接口。
 */
interface CommentRepository {
    /**
     * 获取指定视频的评论列表流。
     */
    fun getComments(videoId: String): Flow<List<CommentItem>>

    /**
     * 对评论执行点赞/取消点赞。
     */
    suspend fun toggleCommentLike(
        videoId: String,
        commentId: String,
    )

    /**
     * 发布新评论。
     */
    suspend fun postComment(
        videoId: String,
        content: String,
    ): CommentItem
}

/**
 * 评论数据仓储实现类（包含内置种子演示数据与内存持久化）。
 */
class CommentRepositoryImpl : CommentRepository {
    private val commentsMap = MutableStateFlow<Map<String, List<CommentItem>>>(emptyMap())

    override fun getComments(videoId: String): Flow<List<CommentItem>> {
        if (!commentsMap.value.containsKey(videoId)) {
            val defaultComments = createSampleComments(videoId)
            commentsMap.value = commentsMap.value + (videoId to defaultComments)
        }
        return MutableStateFlow(commentsMap.value[videoId].orEmpty()).asStateFlow()
    }

    override suspend fun toggleCommentLike(
        videoId: String,
        commentId: String,
    ) {
        val currentList = commentsMap.value[videoId].orEmpty()
        val updated =
            currentList.map { item ->
                if (item.id == commentId) {
                    val newLiked = !item.isLiked
                    val newCount = if (newLiked) item.likeCount + 1 else (item.likeCount - 1).coerceAtLeast(0)
                    item.copy(isLiked = newLiked, likeCount = newCount)
                } else {
                    item
                }
            }
        commentsMap.value = commentsMap.value + (videoId to updated)
    }

    override suspend fun postComment(
        videoId: String,
        content: String,
    ): CommentItem {
        val newComment =
            CommentItem(
                id = "c_${System.currentTimeMillis()}",
                videoId = videoId,
                user =
                    Author(
                        id = "self",
                        nickname = "我 (当前用户)",
                        avatarUrl = "https://picsum.photos/100/100?random=self",
                    ),
                content = content,
                likeCount = 0L,
                isLiked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            )
        val currentList = commentsMap.value[videoId].orEmpty()
        val updated = listOf(newComment) + currentList
        commentsMap.value = commentsMap.value + (videoId to updated)
        return newComment
    }

    private fun createSampleComments(videoId: String): List<CommentItem> =
        listOf(
            CommentItem(
                id = "c1_$videoId",
                videoId = videoId,
                user =
                    Author(
                        id = "u1",
                        nickname = "极客阿强",
                        avatarUrl = "https://picsum.photos/100/100?random=11",
                    ),
                content = "这段视频拍得太绝了！画质清晰，音效也很带感 👍",
                likeCount = 128L,
                isLiked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                replyCount = 2,
                replyList =
                    listOf(
                        CommentItem(
                            id = "r1",
                            videoId = videoId,
                            user = Author(id = "u_author", nickname = "创作者", avatarUrl = "https://picsum.photos/100/100?random=author"),
                            content = "感谢支持！下一期更精彩！",
                            likeCount = 12L,
                            isLiked = false,
                        ),
                    ),
            ),
            CommentItem(
                id = "c2_$videoId",
                videoId = videoId,
                user =
                    Author(
                        id = "u2",
                        nickname = "旅行喵喵",
                        avatarUrl = "https://picsum.photos/100/100?random=12",
                    ),
                content = "求背景音乐歌名！太好听了循环播放中 🎵",
                likeCount = 56L,
                isLiked = false,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
            CommentItem(
                id = "c3_$videoId",
                videoId = videoId,
                user =
                    Author(
                        id = "u3",
                        nickname = "代码诗人",
                        avatarUrl = "https://picsum.photos/100/100?random=13",
                    ),
                content = "Compose + VideoKit 这个体验简直丝滑顺畅！",
                likeCount = 33L,
                isLiked = true,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ),
        )
}
