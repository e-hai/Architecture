package xxx.yyy.zzz.core.pay

import android.content.Context
import com.kit.pay.models.PayKitConfiguration
import com.kit.pay.PayKit as AndroidNativePayKit

/**
 * Android 端支付初始化器。
 */
object PayInitializer {
    fun configure(context: Context) {
        val config =
            PayKitConfiguration(
                subsProductIds = setOf(PayProducts.SUBS_PLUS, PayProducts.SUBS_PRO),
                consumableProductIds = setOf(PayProducts.CONSUMABLE_SKIN_3DAY),
                nonConsumableProductIds = setOf(PayProducts.NON_CONSUMABLE_SKIN),
            )
        AndroidNativePayKit.configure(context, config)

        PayKit.install(
            object : PayDelegate {
                override fun purchase(
                    productId: String,
                    onResult: (Boolean, String?) -> Unit,
                ) {
                    // 原生支付发起由 Activity 宿主结合 PayKit.purchase 执行
                    println("[Pay-Android] purchase request: $productId")
                }

                override fun isSubscribed(productId: String): Boolean = false
            },
        )
    }
}
