package xxx.yyy.zzz.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xxx.yyy.zzz.core.model.MusicTrack

/**
 * 视频配乐网络响应 DTO。
 */
@Serializable
data class MusicTrackResponse(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("artist")
    val artist: String,
    @SerialName("audio_url")
    val audioUrl: String = "",
    @SerialName("cover_url")
    val coverUrl: String = "",
    @SerialName("duration_ms")
    val durationMs: Long = 0L,
)

/**
 * 将 [MusicTrackResponse] DTO 转换为领域模型 [MusicTrack]。
 */
fun MusicTrackResponse.toDomain(): MusicTrack =
    MusicTrack(
        id = id,
        title = title,
        artist = artist,
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        durationMs = durationMs,
    )
