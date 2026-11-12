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
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.pay.PayProducts
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CorePay 应用内购与订阅调试区块。
 */
internal class PayDebugSection : DebugSection {
    override val id: String = "pay"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 60

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        Text("应用内购支付 (CorePay):", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                val sub = CorePay.isSubscribed(PayProducts.SUBS_PRO)
                emitLog(LogTags.PAY, "订阅状态(${PayProducts.SUBS_PRO}): $sub", "I")
            }) { Text("查询订阅状态") }
            ElevatedButton(onClick = {
                emitLog(LogTags.PAY, "发起购买 ${PayProducts.SUBS_PRO}...", "I")
                CorePay.purchase(PayProducts.SUBS_PRO) { success, error ->
                    emitLog(LogTags.PAY, "购买结果: 成功=$success, 错误=$error", if (success) "I" else "E")
                }
            }) { Text("发起订阅购买") }
        }
    }
}
