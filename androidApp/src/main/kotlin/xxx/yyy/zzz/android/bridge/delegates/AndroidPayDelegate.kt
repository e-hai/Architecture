package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import com.kit.pay.PayKit
import com.kit.pay.interfaces.PayKitLogLevel
import com.kit.pay.interfaces.PayKitLogger
import com.kit.pay.models.PayKitConfiguration
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.pay.PayDelegate
import xxx.yyy.zzz.core.pay.PayProducts

/**
 * Android 支付结算委托实现（1:1 透传 PayKit-Android 原生 SDK）。
 */
internal class AndroidPayDelegate(
    private val context: Context,
) : PayDelegate {
    override fun doInit() {
        PayKit.setLogger(
            PayKitLogger { level, tag, message ->
                val formatted = "[$tag] $message"
                when (level) {
                    PayKitLogLevel.DEBUG -> CoreLog.d(LogTags.APP, formatted)
                    PayKitLogLevel.INFO -> CoreLog.i(LogTags.APP, formatted)
                    PayKitLogLevel.WARN -> CoreLog.w(LogTags.APP, formatted)
                    PayKitLogLevel.ERROR -> CoreLog.e(LogTags.APP, formatted)
                }
            },
        )
        val config =
            PayKitConfiguration(
                subsProductIds = setOf(PayProducts.SUBS_PLUS, PayProducts.SUBS_PRO),
                consumableProductIds = setOf(PayProducts.CONSUMABLE_SKIN_3DAY),
                nonConsumableProductIds = setOf(PayProducts.NON_CONSUMABLE_SKIN),
            )
        PayKit.initialize(context, config)
    }

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        CoreLog.d(LogTags.APP, "[Pay-Android] purchase request: $productId")
    }

    override fun isSubscribed(productId: String): Boolean = false
}
