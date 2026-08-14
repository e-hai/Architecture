package xxx.yyy.zzz.feature.comment.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 评论区 ViewModel。
 *
 * @param repository 评论数据仓储
 */
class CommentViewModel(
    private val repository: CommentRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    /**
     * 载入指定视频的评论列表。
     */
    fun loadComments(videoId: String) {
        if (_uiState.value.videoId == videoId && _uiState.value.comments.isNotEmpty()) return

        _uiState.update { it.copy(videoId = videoId, isLoading = true) }
        viewModelScope.launch {
            repository.getComments(videoId).collect { list ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        comments = list,
                        totalCount = list.size.toLong(),
                    )
                }
            }
        }
    }

    /**
     * 更新输入框文案。
     */
    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputContent = text) }
    }

    /**
     * 点赞/取消点赞单条评论。
     */
    fun onLikeClick(commentId: String) {
        val videoId = _uiState.value.videoId
        viewModelScope.launch {
            repository.toggleCommentLike(videoId, commentId)
            // 重新刷新当前列表
            repository.getComments(videoId).collect { list ->
                _uiState.update { it.copy(comments = list) }
            }
        }
    }

    /**
     * 发送新评论。
     */
    fun onSubmitComment() {
        val currentInput = _uiState.value.inputContent.trim()
        val videoId = _uiState.value.videoId
        if (currentInput.isBlank() || videoId.isBlank() || _uiState.value.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                repository.postComment(videoId, currentInput)
                _uiState.update {
                    it.copy(
                        inputContent = "",
                        isSubmitting = false,
                    )
                }
                repository.getComments(videoId).collect { list ->
                    _uiState.update {
                        it.copy(
                            comments = list,
                            totalCount = list.size.toLong(),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = "发送失败，请重试",
                    )
                }
            }
        }
    }
}
