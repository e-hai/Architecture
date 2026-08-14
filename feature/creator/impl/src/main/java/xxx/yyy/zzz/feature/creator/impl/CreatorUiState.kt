package xxx.yyy.zzz.feature.creator.impl

import androidx.compose.runtime.Immutable

/**
 * 创作页步骤模式。
 */
enum class CreatorStep {
    RECORDING,
    PUBLISHING,
}

/**
 * 创作中心 UI 状态。
 *
 * @property step 当前所处步骤（录制 / 发布）
 * @property isRecording 是否正在录制中
 * @property recordProgress 录制进度（0f ~ 1f）
 * @property recordedDurationSeconds 已录制秒数（0 ~ 15s）
 * @property selectedSpeed 当前选中的倍速（0.5f, 1.0f, 2.0f, 3.0f）
 * @property isFrontCamera 是否为前置摄像头
 * @property isFlashOn 闪光灯是否开启
 * @property publishTitle 发布文案标题
 * @property selectedTags 选中的话题标签列表
 * @property isPublic 是否公开可见
 * @property allowComments 是否允许评论
 * @property isPublishing 是否正在发布上传
 * @property publishSuccess 是否发布成功
 * @property errorMsg 错误提示
 */
@Immutable
data class CreatorUiState(
    val step: CreatorStep = CreatorStep.RECORDING,
    val isRecording: Boolean = false,
    val recordProgress: Float = 0f,
    val recordedDurationSeconds: Int = 0,
    val selectedSpeed: Float = 1.0f,
    val isFrontCamera: Boolean = false,
    val isFlashOn: Boolean = false,
    val publishTitle: String = "",
    val selectedTags: List<String> = listOf("生活", "日常"),
    val isPublic: Boolean = true,
    val allowComments: Boolean = true,
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val errorMsg: String? = null,
)
