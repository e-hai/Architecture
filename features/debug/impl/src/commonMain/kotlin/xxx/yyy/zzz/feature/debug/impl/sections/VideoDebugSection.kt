package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.video.CoreVideo
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CoreVideo 视频预加载、磁盘缓存与断点进度调试区块。
 */
internal class VideoDebugSection : DebugSection {
    override val id: String = "video"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 80

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("音视频SDK组件 (CoreVideo):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sampleVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            val sampleVideoId = "video_sample_001"

            ElevatedButton(onClick = {
                CoreVideo.preload(sampleVideoUrl, 2 * 1024 * 1024)
                emitLog(LogTags.VIDEO, "触发视频预加载 (2MB): $sampleVideoUrl", "I")
            }) { Text("预加载视频 (2MB)") }

            ElevatedButton(onClick = {
                val cached = CoreVideo.isCached(sampleVideoUrl)
                val bytes = CoreVideo.getCachedBytes(sampleVideoUrl)
                emitLog(LogTags.VIDEO, "查询视频缓存: cached=$cached, bytes=${bytes / 1024}KB", "I")
            }) { Text("检查视频缓存") }

            ElevatedButton(onClick = {
                val nextPos = (CoreVideo.getProgress(sampleVideoId) + 15000L) % 120000L
                CoreVideo.saveProgress(sampleVideoId, nextPos)
                emitLog(LogTags.VIDEO, "保存播放进度: videoId=$sampleVideoId, pos=${nextPos / 1000}s", "I")
            }) { Text("保存断点进度(+15s)") }

            ElevatedButton(onClick = {
                val pos = CoreVideo.getProgress(sampleVideoId)
                emitLog(LogTags.VIDEO, "读取播放进度: videoId=$sampleVideoId, pos=${pos / 1000}s", "I")
            }) { Text("读取播放进度") }

            ElevatedButton(onClick = {
                CoreVideo.clearCache()
                emitLog(LogTags.VIDEO, "已清空视频磁盘缓存", "W")
            }) { Text("清空视频缓存") }
        }
    }
}
