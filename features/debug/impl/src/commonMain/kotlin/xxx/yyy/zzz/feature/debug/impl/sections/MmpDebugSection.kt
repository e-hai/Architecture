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
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.mmp.MmpEvents
import xxx.yyy.zzz.core.mmp.MmpParams
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CoreMmp 归因统计调试区块。
 */
internal class MmpDebugSection : DebugSection {
    override val id: String = "mmp"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 30

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("归因统计组件 (CoreMmp):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                CoreMmp.setCustomerUserId("mmp_user_99")
                CoreMmp.trackEvent(MmpEvents.PURCHASE, mapOf(MmpParams.SKU to "subs_pro"))
                emitLog(LogTags.MMP, "上报归因事件: purchase (sku=subs_pro)", "I")
            }) { Text("上报 MMP 事件") }
            ElevatedButton(onClick = {
                CoreMmp.trackRevenue(
                    revenue = 9.99,
                    currency = "USD",
                    eventName = MmpEvents.PURCHASE,
                    params = mapOf(MmpParams.SKU to "subs_pro"),
                )
                emitLog(LogTags.MMP, "上报归因收入: 9.99 USD (sku=subs_pro)", "I")
            }) { Text("上报 MMP 收入") }
            ElevatedButton(onClick = {
                val attr = CoreMmp.getAttribution()
                val summary =
                    if (attr == null) {
                        "暂未就绪（需有效 AppsFlyer Key / 真机回调）"
                    } else {
                        "isOrganic=${attr.isOrganic}, network=${attr.network}, campaign=${attr.campaign}"
                    }
                emitLog(LogTags.MMP, "当前归因快照: $summary", "I")
            }) { Text("查看归因快照") }
        }
    }
}
