package com.shortvideo.app.feature.comment.impl

import androidx.compose.runtime.Immutable
import com.shortvideo.app.core.model.CommentItem

/**
 * 评论区 UI 状态。
 *
 * @property videoId 当前视频 ID
 * @property isLoading 是否正在加载中
 * @property comments 评论列表
 * @property totalCount 评论总数
 * @property inputContent 输入框当前内容
 * @property isSubmitting 是否正在提交评论
 * @property errorMsg 错误提示信息
 */
@Immutable
data class CommentUiState(
    val videoId: String = "",
    val isLoading: Boolean = false,
    val comments: List<CommentItem> = emptyList(),
    val totalCount: Long = 0L,
    val inputContent: String = "",
    val isSubmitting: Boolean = false,
    val errorMsg: String? = null,
)
