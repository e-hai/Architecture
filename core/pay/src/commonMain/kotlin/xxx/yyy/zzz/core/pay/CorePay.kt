package xxx.yyy.zzz.core.pay

/**
 * 应用内购买与订阅商品目录配置。
 * 建议由宿主配置文件（如 dev.properties / xcconfig）或远端配置下发后在初始化阶段注入。
 */
data class PayProductCatalog(
    val subsProductIds: Set<String> = emptySet(),
    val consumableProductIds: Set<String> = emptySet(),
    val nonConsumableProductIds: Set<String> = emptySet(),
) {
    val allProductIds: Set<String>
        get() = subsProductIds + consumableProductIds + nonConsumableProductIds

    companion object {
        /**
         * 从逗号分隔的配置文本解析商品目录（自动忽略空白项）。
         */
        fun fromCsv(
            subsCsv: String = "",
            consumableCsv: String = "",
            nonConsumableCsv: String = "",
        ): PayProductCatalog =
            PayProductCatalog(
                subsProductIds = parseCsv(subsCsv),
                consumableProductIds = parseCsv(consumableCsv),
                nonConsumableProductIds = parseCsv(nonConsumableCsv),
            )

        private fun parseCsv(csv: String): Set<String> =
            csv
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
    }
}

/**
 * 商店商品类型。
 */
enum class PayProductType {
    SUBS,
    INAPP,
}

/**
 * 跨平台商店商品展示详情（由 Google Play / App Store 动态拉取）。
 */
data class PayProductInfo(
    val productId: String,
    val type: PayProductType,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long = 0L,
    val priceCurrencyCode: String = "",
    val basePlanId: String? = null,
    val subscriptionPeriod: String? = null,
    val hasFreeTrial: Boolean = false,
    val freeTrialPeriod: String? = null,
)

/**
 * 跨平台支付结算委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 PayKit SDK。
 */
interface PayDelegate {
    /**
     * 初始化支付 SDK 与商品目录配置。
     */
    fun doInit(catalog: PayProductCatalog)

    /**
     * 从商店（Google Play / App Store）动态拉取商品详细信息（标题、本地化价格、试用期等）。
     */
    fun fetchProducts(
        productIds: Set<String>,
        onResult: (List<PayProductInfo>, String?) -> Unit,
    )

    fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    )

    fun isSubscribed(productId: String): Boolean

    /**
     * 恢复历史购买与订阅权益。
     */
    fun restorePurchases(onResult: (Boolean, String?) -> Unit)
}

/**
 * 默认支付兜底实现。
 */
object DefaultPayDelegate : PayDelegate {
    override fun doInit(catalog: PayProductCatalog) = Unit

    override fun fetchProducts(
        productIds: Set<String>,
        onResult: (List<PayProductInfo>, String?) -> Unit,
    ) {
        println("[Pay] FetchProducts: $productIds (NoOp)")
        onResult(emptyList(), "PayKit is in default NoOp mode")
    }

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        println("[Pay] Purchase request: $productId (NoOp)")
        onResult(false, "PayKit is in default NoOp mode")
    }

    override fun isSubscribed(productId: String): Boolean = false

    override fun restorePurchases(onResult: (Boolean, String?) -> Unit) {
        println("[Pay] RestorePurchases (NoOp)")
        onResult(false, "PayKit is in default NoOp mode")
    }
}

/**
 * 跨平台静态支付门面（CorePay）。
 */
object CorePay : PayDelegate {
    private var delegate: PayDelegate = DefaultPayDelegate
    private var catalog: PayProductCatalog = PayProductCatalog()

    /**
     * 挂载平台特定的支付委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的支付委托接口
     */
    fun install(delegate: PayDelegate) {
        this.delegate = delegate
    }

    /**
     * 获取当前初始化注入的商品目录配置。
     */
    fun getCatalog(): PayProductCatalog = catalog

    override fun doInit(catalog: PayProductCatalog) {
        this.catalog = catalog
        delegate.doInit(catalog)
    }

    override fun fetchProducts(
        productIds: Set<String>,
        onResult: (List<PayProductInfo>, String?) -> Unit,
    ) = delegate.fetchProducts(productIds, onResult)

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) = delegate.purchase(productId, onResult)

    override fun isSubscribed(productId: String): Boolean = delegate.isSubscribed(productId)

    override fun restorePurchases(onResult: (Boolean, String?) -> Unit) = delegate.restorePurchases(onResult)
}
