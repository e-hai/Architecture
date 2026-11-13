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
import xxx.yyy.zzz.core.push.CorePush
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CorePush 推送通知调试区块。
 */
internal class PushDebugSection : DebugSection {
    override val id: String = "push"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 70

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("消息推送组件 (CorePush):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                CorePush.registerPush()
                val granted = CorePush.hasPushPermission()
                val tokenPreview = CorePush.getPushToken()?.take(16)?.let { "$it..." } ?: "等待系统回传"
                emitLog(LogTags.PUSH, "已触发推送注册，当前权限=$granted, Token=$tokenPreview", "I")
            }) { Text("申请通知权限并注册") }

            ElevatedButton(onClick = {
                val granted = CorePush.hasPushPermission()
                val token = CorePush.getPushToken() ?: "<empty>"
                emitLog(LogTags.PUSH, "通知权限=$granted, Token=$token", "I")
            }) { Text("查询推送状态/Token") }
        }
    }
}
