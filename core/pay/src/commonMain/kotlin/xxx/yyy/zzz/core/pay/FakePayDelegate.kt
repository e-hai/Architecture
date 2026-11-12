package xxx.yyy.zzz.core.pay

/**
 * 内存可编程应用内购模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakePayDelegate : PayDelegate {
    var initCount = 0
    val activeSubscriptions = mutableSetOf<String>()
    var nextPurchaseSuccess = true
    var nextPurchaseError: String? = null

    override fun doInit() {
        initCount++
    }

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        if (nextPurchaseSuccess) {
            activeSubscriptions.add(productId)
            onResult(true, null)
        } else {
            onResult(false, nextPurchaseError ?: "Purchase failed")
        }
    }

    override fun isSubscribed(productId: String): Boolean = activeSubscriptions.contains(productId)

    fun clear() {
        initCount = 0
        activeSubscriptions.clear()
        nextPurchaseSuccess = true
        nextPurchaseError = null
    }
}
