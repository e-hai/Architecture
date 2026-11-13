package xxx.yyy.zzz.core.mmp

/**
 * 跨平台归一化归因结果（由原生 MmpKit 归因回调映射而来）。
 */
data class MmpAttribution(
    val network: String? = null,
    val campaign: String? = null,
    val adGroup: String? = null,
    val creative: String? = null,
    val clickLabel: String? = null,
    val isOrganic: Boolean = false,
)

/**
 * 跨平台归一化深度链接与延迟深度链接结果（含 MmpKit 1.0.1 Direct Deep Link 解析结果）。
 */
data class MmpDeepLink(
    val deepLinkValue: String? = null,
    val params: Map<String, String> = emptyMap(),
    val isDeferred: Boolean = false,
)

/**
 * 跨平台 MMP 归因委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 MmpKit SDK。
 */
interface MmpDelegate {
    /**
     * 初始化归因 SDK。
     *
     * @param appToken 平台归因 App 标识/密钥
     */
    fun doInit(appToken: String)

    fun trackEvent(
        eventName: String,
        params: Map<String, Any?> = emptyMap(),
    )

    /**
     * 上报归因收入事件。
     *
     * @param revenue 收入金额
     * @param currency ISO 4217 货币代码（如 "USD"）
     * @param eventName 归因收入事件名（默认 [MmpEvents.PURCHASE]）
     * @param params 附加事件参数
     */
    fun trackRevenue(
        revenue: Double,
        currency: String,
        eventName: String = MmpEvents.PURCHASE,
        params: Map<String, Any?> = emptyMap(),
    ) = Unit

    fun setCustomerUserId(userId: String?)

    /**
     * 获取当前已缓存的归因信息，未就绪时返回 null。
     */
    fun getAttribution(): MmpAttribution? = null

    /**
     * 注册归因结果监听器；若缓存已就绪应立即重放最新结果。
     */
    fun setAttributionListener(listener: ((MmpAttribution) -> Unit)?) = Unit

    /**
     * 注册深度链接监听器（同时接收延迟深链与直接深链解析结果）。
     */
    fun setDeepLinkListener(listener: ((MmpDeepLink) -> Unit)?) = Unit
}

/**
 * 默认控制台兜底实现。
 */
object DefaultMmpDelegate : MmpDelegate {
    override fun doInit(appToken: String) = Unit

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        println("[Mmp] TrackEvent: $eventName, Params: $params")
    }

    override fun trackRevenue(
        revenue: Double,
        currency: String,
        eventName: String,
        params: Map<String, Any?>,
    ) {
        println("[Mmp] TrackRevenue: $eventName, revenue=$revenue $currency, Params: $params")
    }

    override fun setCustomerUserId(userId: String?) {
        println("[Mmp] SetCustomerUserId: $userId")
    }
}

/**
 * 跨平台静态归因门面（CoreMmp）。
 */
object CoreMmp : MmpDelegate {
    private var delegate: MmpDelegate = DefaultMmpDelegate

    /**
     * 挂载平台特定的归因委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的归因委托接口
     */
    fun install(delegate: MmpDelegate) {
        this.delegate = delegate
    }

    override fun doInit(appToken: String) = delegate.doInit(appToken)

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) = delegate.trackEvent(eventName, params)

    override fun trackRevenue(
        revenue: Double,
        currency: String,
        eventName: String,
        params: Map<String, Any?>,
    ) = delegate.trackRevenue(revenue, currency, eventName, params)

    override fun setCustomerUserId(userId: String?) = delegate.setCustomerUserId(userId)

    override fun getAttribution(): MmpAttribution? = delegate.getAttribution()

    override fun setAttributionListener(listener: ((MmpAttribution) -> Unit)?) = delegate.setAttributionListener(listener)

    override fun setDeepLinkListener(listener: ((MmpDeepLink) -> Unit)?) = delegate.setDeepLinkListener(listener)
}
