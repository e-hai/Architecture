package xxx.yyy.zzz.core.mmp

/**
 * 内存可编程 MMP 归因模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeMmpDelegate : MmpDelegate {
    data class MmpEvent(
        val name: String,
        val params: Map<String, Any?>,
    )

    data class MmpRevenueRecord(
        val revenue: Double,
        val currency: String,
        val eventName: String,
        val params: Map<String, Any?>,
    )

    var initializedToken: String? = null
    var recordedCustomerUserId: String? = null
    val trackedEvents = mutableListOf<MmpEvent>()
    val trackedRevenues = mutableListOf<MmpRevenueRecord>()

    private var cachedAttribution: MmpAttribution? = null
    private var pendingDeepLink: MmpDeepLink? = null
    private var attributionListener: ((MmpAttribution) -> Unit)? = null
    private var deepLinkListener: ((MmpDeepLink) -> Unit)? = null

    override fun doInit(appToken: String) {
        initializedToken = appToken
    }

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        trackedEvents.add(MmpEvent(eventName, params))
    }

    override fun trackRevenue(
        revenue: Double,
        currency: String,
        eventName: String,
        params: Map<String, Any?>,
    ) {
        trackedRevenues.add(MmpRevenueRecord(revenue, currency, eventName, params))
    }

    override fun setCustomerUserId(userId: String?) {
        recordedCustomerUserId = userId
    }

    override fun getAttribution(): MmpAttribution? = cachedAttribution

    override fun setAttributionListener(listener: ((MmpAttribution) -> Unit)?) {
        attributionListener = listener
        val cached = cachedAttribution
        if (listener != null && cached != null) {
            listener.invoke(cached)
        }
    }

    override fun setDeepLinkListener(listener: ((MmpDeepLink) -> Unit)?) {
        deepLinkListener = listener
        val pending = pendingDeepLink
        if (listener != null && pending != null) {
            pendingDeepLink = null
            listener.invoke(pending)
        }
    }

    fun emitAttribution(attribution: MmpAttribution) {
        cachedAttribution = attribution
        attributionListener?.invoke(attribution)
    }

    fun emitDeepLink(deepLink: MmpDeepLink) {
        val listener = deepLinkListener
        if (listener != null) {
            listener.invoke(deepLink)
        } else {
            pendingDeepLink = deepLink
        }
    }

    fun clear() {
        initializedToken = null
        recordedCustomerUserId = null
        trackedEvents.clear()
        trackedRevenues.clear()
        cachedAttribution = null
        pendingDeepLink = null
        attributionListener = null
        deepLinkListener = null
    }
}
