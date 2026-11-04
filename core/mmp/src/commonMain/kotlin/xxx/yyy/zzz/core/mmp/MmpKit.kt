package xxx.yyy.zzz.core.mmp

/**
 * 跨平台 MMP 归因委托协议。
 */
interface MmpDelegate {
    fun trackEvent(
        eventName: String,
        params: Map<String, Any?> = emptyMap(),
    )

    fun setCustomerUserId(userId: String?)
}

/**
 * 默认控制台兜底实现。
 */
object DefaultMmpDelegate : MmpDelegate {
    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        println("[Mmp] TrackEvent: $eventName, Params: $params")
    }

    override fun setCustomerUserId(userId: String?) {
        println("[Mmp] SetCustomerUserId: $userId")
    }
}

/**
 * 跨平台静态归因门面。
 */
object MmpKit : MmpDelegate {
    private var delegate: MmpDelegate = DefaultMmpDelegate

    fun install(platformDelegate: MmpDelegate) {
        delegate = platformDelegate
    }

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) = delegate.trackEvent(eventName, params)

    override fun setCustomerUserId(userId: String?) = delegate.setCustomerUserId(userId)
}
