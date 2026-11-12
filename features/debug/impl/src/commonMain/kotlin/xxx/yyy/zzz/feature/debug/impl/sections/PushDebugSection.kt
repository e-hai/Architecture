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
                CorePush.setPushToken("mock_fcm_token_12345")
                emitLog(LogTags.PUSH, "已调用 registerPush，Token 设置为 mock_fcm_token_12345", "I")
            }) { Text("注册远程推送") }
        }
    }
}
