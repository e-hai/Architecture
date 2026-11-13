package xxx.yyy.zzz.feature.debug.impl.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.pay.PayProductInfo
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

/**
 * CorePay 应用内购与订阅调试区块。
 * 从 CorePay.getCatalog() 读取配置的商品 ID 并从商店动态拉取商品标题与本地化价格。
 */
internal class PayDebugSection : DebugSection {
    override val id: String = "pay"
    override val category: DebugCategory = DebugCategory.KITS
    override val order: Int = 60

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(emitLog: (tag: String, message: String, level: String) -> Unit) {
        var products by remember { mutableStateOf<List<PayProductInfo>>(emptyList()) }
        val catalog = remember { CorePay.getCatalog() }

        Text(
            "应用内购支付 (CorePay, 已配 ${catalog.allProductIds.size} 个商品):",
            style = MaterialTheme.typography.labelMedium,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedButton(onClick = {
                val ids = CorePay.getCatalog().allProductIds
                emitLog(LogTags.PAY, "正在向商店拉取商品: $ids", "I")
                CorePay.fetchProducts(ids) { fetched, error ->
                    products = fetched
                    if (error != null) {
                        emitLog(LogTags.PAY, "拉取商品失败: $error", "E")
                    } else {
                        emitLog(LogTags.PAY, "成功拉取 ${fetched.size} 个商品", "I")
                    }
                }
            }) { Text("拉取商店商品") }

            ElevatedButton(onClick = {
                val subsIds = CorePay.getCatalog().subsProductIds
                if (subsIds.isEmpty()) {
                    emitLog(LogTags.PAY, "未配置任何订阅商品 ID", "W")
                } else {
                    subsIds.forEach { id ->
                        val sub = CorePay.isSubscribed(id)
                        emitLog(LogTags.PAY, "订阅状态($id): $sub", "I")
                    }
                }
            }) { Text("查询订阅状态") }

            ElevatedButton(onClick = {
                emitLog(LogTags.PAY, "发起恢复购买...", "I")
                CorePay.restorePurchases { success, error ->
                    emitLog(LogTags.PAY, "恢复购买结果: 成功=$success, 错误=$error", if (success) "I" else "E")
                }
            }) { Text("恢复购买") }
        }

        if (products.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                products.forEach { product ->
                    ElevatedButton(onClick = {
                        emitLog(LogTags.PAY, "发起购买 ${product.productId} (${product.formattedPrice})...", "I")
                        CorePay.purchase(product.productId) { success, error ->
                            emitLog(
                                LogTags.PAY,
                                "购买结果(${product.productId}): 成功=$success, 错误=$error",
                                if (success) "I" else "E",
                            )
                        }
                    }) {
                        Text("购买 ${product.title} (${product.formattedPrice})")
                    }
                }
            }
        }
    }
}
