package xxx.yyy.zzz.android.bridge.delegates

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.kit.pay.PayKit
import com.kit.pay.interfaces.PayKitError
import com.kit.pay.interfaces.PayKitLogLevel
import com.kit.pay.interfaces.PayKitLogger
import com.kit.pay.interfaces.PurchaseCallback
import com.kit.pay.models.CustomerInfo
import com.kit.pay.models.PayKitConfiguration
import com.kit.pay.models.ProductType
import com.kit.pay.models.StoreProduct
import com.kit.pay.models.StoreTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.pay.PayDelegate
import xxx.yyy.zzz.core.pay.PayProductCatalog
import xxx.yyy.zzz.core.pay.PayProductInfo
import xxx.yyy.zzz.core.pay.PayProductType
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 支付结算委托实现（1:1 透传 PayKit-Android 原生 SDK）。
 */
internal class AndroidPayDelegate(
    private val application: Application,
) : PayDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val cachedProducts = ConcurrentHashMap<String, StoreProduct>()
    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {
                    currentActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (currentActivity === activity) currentActivity = null
                }

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {
                    if (currentActivity === activity) currentActivity = null
                }
            },
        )
    }

    override fun doInit(catalog: PayProductCatalog) {
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
                subsProductIds = catalog.subsProductIds,
                consumableProductIds = catalog.consumableProductIds,
                nonConsumableProductIds = catalog.nonConsumableProductIds,
            )
        PayKit.initialize(application, config)
    }

    override fun fetchProducts(
        productIds: Set<String>,
        onResult: (List<PayProductInfo>, String?) -> Unit,
    ) {
        if (productIds.isEmpty()) {
            onResult(emptyList(), "No productIds configured")
            return
        }
        scope.launch {
            val result =
                runCatching { PayKit.shared.getProducts(productIds) }.getOrElse {
                    onResult(emptyList(), it.message ?: "PayKit is not initialized")
                    return@launch
                }
            result
                .onSuccess { storeProducts ->
                    storeProducts.forEach { cachedProducts[it.productId] = it }
                    val mapped =
                        storeProducts.map { product ->
                            PayProductInfo(
                                productId = product.productId,
                                type =
                                    when (product.type) {
                                        ProductType.SUBS -> PayProductType.SUBS
                                        ProductType.INAPP -> PayProductType.INAPP
                                    },
                                title = product.title,
                                description = product.description,
                                formattedPrice = product.price,
                                priceAmountMicros = product.priceAmountMicros,
                                priceCurrencyCode = product.priceCurrencyCode,
                                basePlanId = product.basePlanId,
                                hasFreeTrial = product.hasFreeTrial,
                                freeTrialPeriod = product.freeTrialPeriod,
                            )
                        }
                    onResult(mapped, null)
                }.onFailure { error ->
                    onResult(emptyList(), error.message ?: "Failed to query products")
                }
        }
    }

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val activity = currentActivity
        if (activity == null) {
            onResult(false, "No foreground Activity")
            return
        }
        val cached = cachedProducts[productId]
        if (cached != null) {
            launchPurchase(activity, cached, onResult)
            return
        }
        scope.launch {
            val result =
                runCatching { PayKit.shared.getProducts(setOf(productId)) }.getOrElse {
                    onResult(false, it.message ?: "PayKit is not initialized")
                    return@launch
                }
            result
                .onSuccess { list ->
                    val target = list.firstOrNull { it.productId == productId }
                    if (target != null) {
                        cachedProducts[productId] = target
                        launchPurchase(activity, target, onResult)
                    } else {
                        onResult(false, "Product not found in Play Store: $productId")
                    }
                }.onFailure { error ->
                    onResult(false, error.message ?: "Failed to load product: $productId")
                }
        }
    }

    private fun launchPurchase(
        activity: Activity,
        storeProduct: StoreProduct,
        onResult: (Boolean, String?) -> Unit,
    ) {
        PayKit.shared.purchase(
            activity = activity,
            storeProduct = storeProduct,
            callback =
                object : PurchaseCallback {
                    override fun onCompleted(
                        storeTransaction: StoreTransaction,
                        customerInfo: CustomerInfo,
                    ) {
                        onResult(true, null)
                    }

                    override fun onPending(storeTransaction: StoreTransaction) {
                        onResult(false, "Purchase is pending confirmation")
                    }

                    override fun onError(
                        error: PayKitError,
                        userCancelled: Boolean,
                    ) {
                        val msg = if (userCancelled) "User cancelled" else error.message
                        onResult(false, msg)
                    }
                },
        )
    }

    override fun isSubscribed(productId: String): Boolean =
        runCatching { PayKit.shared.findActiveSubscription(productId) != null }.getOrDefault(false)

    override fun restorePurchases(onResult: (Boolean, String?) -> Unit) {
        scope.launch {
            val result =
                runCatching { PayKit.shared.restorePurchases() }.getOrElse {
                    onResult(false, it.message ?: "PayKit is not initialized")
                    return@launch
                }
            result
                .onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message ?: "Restore purchases failed") }
        }
    }
}
