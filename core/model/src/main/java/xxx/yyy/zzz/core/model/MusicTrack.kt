package xxx.yyy.zzz.core.model

import kotlinx.serialization.Serializable

/**
 * 视频原声/背景音乐领域模型。
 *
 * @property id 音乐唯一标识 ID
 * @property title 音乐标题
 * @property artist 艺术家/创作者名称
 * @property audioUrl 音频播放地址
 * @property coverUrl 唱片封面地址
 * @property durationMs 音频总时长（毫秒）
 */
@Serializable
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val audioUrl: String = "",
    val coverUrl: String = "",
    val durationMs: Long = 0L,
)
