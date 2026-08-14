package xxx.yyy.zzz.feature.creator.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.analytics.Analytics
import com.kit.log.LogKit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 创作中心 ViewModel。
 *
 * @param repository 创作仓储
 */
class CreatorViewModel(
    private val repository: CreatorRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreatorUiState())
    val uiState: StateFlow<CreatorUiState> = _uiState.asStateFlow()

    private var recordJob: Job? = null
    private val maxRecordSeconds = 15

    /**
     * 开始/暂停录制。
     */
    fun toggleRecord() {
        if (_uiState.value.isRecording) {
            pauseRecord()
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        _uiState.update { it.copy(isRecording = true) }
        Analytics.logEvent("creator_record_start") {
            param("speed", _uiState.value.selectedSpeed.toString())
        }
        LogKit.d("CreatorViewModel", "Record started")

        recordJob?.cancel()
        recordJob =
            viewModelScope.launch {
                val stepMillis = 100L
                val totalSteps = (maxRecordSeconds * 1000L) / stepMillis
                var currentStep = (_uiState.value.recordProgress * totalSteps).toLong()

                while (currentStep < totalSteps && _uiState.value.isRecording) {
                    delay(stepMillis)
                    currentStep++
                    val progress = currentStep.toFloat() / totalSteps
                    val durationSec = (currentStep * stepMillis / 1000).toInt()
                    _uiState.update {
                        it.copy(
                            recordProgress = progress.coerceIn(0f, 1f),
                            recordedDurationSeconds = durationSec,
                        )
                    }
                }

                if (currentStep >= totalSteps) {
                    _uiState.update { it.copy(isRecording = false) }
                    onProceedToPublish()
                }
            }
    }

    private fun pauseRecord() {
        _uiState.update { it.copy(isRecording = false) }
        recordJob?.cancel()
        LogKit.d("CreatorViewModel", "Record paused")
    }

    /**
     * 重置录制进度。
     */
    fun resetRecord() {
        recordJob?.cancel()
        _uiState.update {
            it.copy(
                isRecording = false,
                recordProgress = 0f,
                recordedDurationSeconds = 0,
            )
        }
    }

    /**
     * 选择拍摄倍速。
     */
    fun onSpeedSelected(speed: Float) {
        _uiState.update { it.copy(selectedSpeed = speed) }
    }

    /**
     * 翻转前后置摄像头。
     */
    fun onFlipCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    /**
     * 开关闪光灯。
     */
    fun onToggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    /**
     * 进入发布编辑页。
     */
    fun onProceedToPublish() {
        _uiState.update { it.copy(step = CreatorStep.PUBLISHING, isRecording = false) }
        recordJob?.cancel()
    }

    /**
     * 返回录制页。
     */
    fun onBackToRecord() {
        _uiState.update { it.copy(step = CreatorStep.RECORDING) }
    }

    /**
     * 编辑发布标题文案。
     */
    fun onTitleChange(text: String) {
        _uiState.update { it.copy(publishTitle = text) }
    }

    /**
     * 切换选择话题标签。
     */
    fun onTagToggle(tag: String) {
        val currentTags = _uiState.value.selectedTags
        val updated = if (currentTags.contains(tag)) currentTags - tag else currentTags + tag
        _uiState.update { it.copy(selectedTags = updated) }
    }

    /**
     * 切换公开/私密可见性。
     */
    fun onTogglePublic(isPublic: Boolean) {
        _uiState.update { it.copy(isPublic = isPublic) }
    }

    /**
     * 执行发布。
     */
    fun onPublish() {
        if (_uiState.value.isPublishing) return

        _uiState.update { it.copy(isPublishing = true) }
        viewModelScope.launch {
            try {
                repository.publishVideo(
                    title = _uiState.value.publishTitle,
                    tags = _uiState.value.selectedTags,
                    isPublic = _uiState.value.isPublic,
                )
                Analytics.logEvent("creator_video_published") {
                    param(
                        "tags_count",
                        _uiState.value.selectedTags.size
                            .toString(),
                    )
                }
                _uiState.update { it.copy(isPublishing = false, publishSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        errorMsg = "发布失败，请重试",
                    )
                }
            }
        }
    }
}
