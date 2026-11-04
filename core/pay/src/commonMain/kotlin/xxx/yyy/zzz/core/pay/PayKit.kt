package xxx.yyy.zzz.core.pay

/**
 * 跨平台支付结算委托协议。
 */
interface PayDelegate {
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
 * 跨平台静态支付门面。
 */
object PayKit : PayDelegate {
    private var delegate: PayDelegate = DefaultPayDelegate

    fun install(platformDelegate: PayDelegate) {
        delegate = platformDelegate
    }

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) = delegate.purchase(productId, onResult)

    override fun isSubscribed(productId: String): Boolean = delegate.isSubscribed(productId)
}
