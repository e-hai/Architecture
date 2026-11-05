package xxx.yyy.zzz.core.pay

/**
 * 跨平台支付结算委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 PayKit SDK。
 */
interface PayDelegate {
    /**
     * 初始化支付 SDK 与产品配置。
     */
    fun doInit()

    fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    )

    fun isSubscribed(productId: String): Boolean
}

/**
 * 默认支付兜底实现。
 */
object DefaultPayDelegate : PayDelegate {
    override fun doInit() = Unit

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        println("[Pay] Purchase request: $productId (NoOp)")
        onResult(false, "PayKit is in default NoOp mode")
    }

    override fun isSubscribed(productId: String): Boolean = false
}

/**
 * 跨平台静态支付门面（CorePay）。
 */
object CorePay : PayDelegate {
    private var delegate: PayDelegate = DefaultPayDelegate

    /**
     * 挂载平台特定的支付委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的支付委托接口
     */
    fun install(delegate: PayDelegate) {
        this.delegate = delegate
    }

    override fun doInit() = delegate.doInit()

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) = delegate.purchase(productId, onResult)

    override fun isSubscribed(productId: String): Boolean = delegate.isSubscribed(productId)
}
