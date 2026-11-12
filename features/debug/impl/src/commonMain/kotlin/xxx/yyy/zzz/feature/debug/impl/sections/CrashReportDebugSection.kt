package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.crashreport.CoreCrashReport
import xxx.yyy.zzz.core.crashreport.CrashReportKeys
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CoreCrashReport 崩溃分析调试区块。
 */
internal class CrashReportDebugSection : DebugSection {
    override val id: String = "crashreport"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 10

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("崩溃分析组件 (CoreCrashReport):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                CoreCrashReport.setUserId("user_debug_001")
                CoreCrashReport.setCustomKey(CrashReportKeys.FLAVOR, "debug_dev")
                emitLog(LogTags.CRASH, "已设置 userId=user_debug_001, key=debug_dev", "I")
            }) { Text("设置用户与标签") }
            ElevatedButton(onClick = {
                CoreCrashReport.recordException(RuntimeException("调试模拟非致命异常"))
                emitLog(LogTags.CRASH, "非致命异常已记录并上报", "W")
            }) { Text("上报非致命异常") }
        }
    }
}
