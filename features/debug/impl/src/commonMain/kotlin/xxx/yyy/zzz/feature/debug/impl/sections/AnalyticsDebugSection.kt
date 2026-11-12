package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.analytics.AnalyticsParams
import xxx.yyy.zzz.core.analytics.CoreAnalytics
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CoreAnalytics 数据统计埋点调试区块。
 */
internal class AnalyticsDebugSection : DebugSection {
    override val id: String = "analytics"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 20

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("数据统计埋点 (CoreAnalytics):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                CoreAnalytics.logEvent(
                    AnalyticsEvents.Common.BUTTON_CLICK,
                    mapOf(AnalyticsParams.ITEM_ID to "debug_btn"),
                )
                emitLog(LogTags.ANALYTICS, "上报埋点事件: button_click (item_id=debug_btn)", "I")
            }) { Text("上报埋点事件") }
        }
    }
}
