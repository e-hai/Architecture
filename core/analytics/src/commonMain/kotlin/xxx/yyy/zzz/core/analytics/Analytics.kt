package xxx.yyy.zzz.core.analytics

/**
 * 跨平台数据统计委托协议。
 */
interface AnalyticsDelegate {
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
 * 跨平台静态数据埋点门面。
 */
object Analytics : AnalyticsDelegate {
    private var delegate: AnalyticsDelegate = DefaultAnalyticsDelegate

    fun install(platformDelegate: AnalyticsDelegate) {
        delegate = platformDelegate
    }

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
