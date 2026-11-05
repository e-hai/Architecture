package xxx.yyy.zzz.core.analytics

/**
 * 跨平台数据统计委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 AnalyticsKit SDK。
 */
interface AnalyticsDelegate {
    /**
     * 初始化数据统计 SDK。
     */
    fun doInit()

    fun logEvent(
        name: String,
        params: Map<String, Any?> = emptyMap(),
    )

    fun setUserId(userId: String?)

    fun setUserProperty(
        name: String,
        value: String?,
    )
}

/**
 * 默认控制台打印实现。
 */
object DefaultAnalyticsDelegate : AnalyticsDelegate {
    override fun doInit() = Unit

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        println("[Analytics] Event: $name, Params: $params")
    }

    override fun setUserId(userId: String?) {
        println("[Analytics] SetUserId: $userId")
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        println("[Analytics] SetUserProperty: $name = $value")
    }
}

/**
 * 跨平台静态数据埋点门面（CoreAnalytics）。
 */
object CoreAnalytics : AnalyticsDelegate {
    private var delegate: AnalyticsDelegate = DefaultAnalyticsDelegate

    /**
     * 挂载平台特定的数据统计委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的数据统计委托接口
     */
    fun install(delegate: AnalyticsDelegate) {
        this.delegate = delegate
    }

    override fun doInit() = delegate.doInit()

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) = delegate.logEvent(name, params)

    override fun setUserId(userId: String?) = delegate.setUserId(userId)

    override fun setUserProperty(
        name: String,
        value: String?,
    ) = delegate.setUserProperty(name, value)
}
