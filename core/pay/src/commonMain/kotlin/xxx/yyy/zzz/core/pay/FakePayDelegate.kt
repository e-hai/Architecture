package xxx.yyy.zzz.core.pay

/**
 * 内存可编程应用内购模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakePayDelegate : PayDelegate {
    var initCount = 0
    var lastCatalog: PayProductCatalog? = null
    val activeSubscriptions = mutableSetOf<String>()
    val stubbedProducts = mutableListOf<PayProductInfo>()
    var nextFetchError: String? = null
    var nextPurchaseSuccess = true
    var nextPurchaseError: String? = null
    var nextRestoreSuccess = true
    var nextRestoreError: String? = null

    override fun doInit(catalog: PayProductCatalog) {
        initCount++
        lastCatalog = catalog
    }

    override fun fetchProducts(
        productIds: Set<String>,
        onResult: (List<PayProductInfo>, String?) -> Unit,
    ) {
        val error = nextFetchError
        if (error != null) {
            onResult(emptyList(), error)
            return
        }
        val filtered =
            if (productIds.isEmpty()) {
                stubbedProducts.toList()
            } else {
                stubbedProducts.filter { it.productId in productIds }
            }
        onResult(filtered, null)
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

    override fun restorePurchases(onResult: (Boolean, String?) -> Unit) {
        if (nextRestoreSuccess) {
            onResult(true, null)
        } else {
            onResult(false, nextRestoreError ?: "Restore failed")
        }
    }

    fun clear() {
        initCount = 0
        lastCatalog = null
        activeSubscriptions.clear()
        stubbedProducts.clear()
        nextFetchError = null
        nextPurchaseSuccess = true
        nextPurchaseError = null
        nextRestoreSuccess = true
        nextRestoreError = null
    }
}
