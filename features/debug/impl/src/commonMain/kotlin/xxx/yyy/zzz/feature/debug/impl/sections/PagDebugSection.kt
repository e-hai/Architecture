package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.pag.CorePag
import xxx.yyy.zzz.core.pag.PagSources
import xxx.yyy.zzz.core.pag.PagView
import xxx.yyy.zzz.core.pag.rememberPagState
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CorePag 跨平台动效调试区块（严格仅通过 `:core:pag` 门面导出组件调用）。
 */
internal class PagDebugSection : DebugSection {
    override val id: String = "pag"
    override val category: DebugCategory = DebugCategory.MEDIA_ANIMATION
    override val order: Int = 10

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        val pagState =
            rememberPagState(
                source = PagSources.asset("like.pag"),
                autoPlay = true,
                repeatCount = 0,
            )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PagView(
                state = pagState,
                modifier =
                    Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "动效状态: ${if (pagState.isPlaying) "播放中" else "暂停"} (${if (pagState.isLoaded) "已装载" else "加载中"})",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "播放进度: ${(pagState.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                pagState.play()
                emitLog(LogTags.PAG, "PAG动效: 播放 (like.pag)", "I")
            }) { Text("播放") }

            ElevatedButton(onClick = {
                pagState.pause()
                emitLog(LogTags.PAG, "PAG动效: 暂停", "I")
            }) { Text("暂停") }

            ElevatedButton(onClick = {
                pagState.stop()
                emitLog(LogTags.PAG, "PAG动效: 停止重置", "I")
            }) { Text("停止") }

            ElevatedButton(onClick = {
                val next = (pagState.progress + 0.25).let { if (it > 1.0) 0.0 else it }
                pagState.seekTo(next)
                emitLog(LogTags.PAG, "PAG动效: 跳转进度 ${(next * 100).toInt()}%", "I")
            }) { Text("+25%") }

            ElevatedButton(onClick = {
                CorePag.clearCache()
                emitLog(LogTags.PAG, "已清空 PAG 磁盘缓存", "W")
            }) { Text("清空缓存") }
        }
    }
}
