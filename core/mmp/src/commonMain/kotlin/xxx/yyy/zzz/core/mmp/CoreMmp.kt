package xxx.yyy.zzz.core.mmp

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

    fun setCustomerUserId(userId: String?)
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

    override fun setCustomerUserId(userId: String?) = delegate.setCustomerUserId(userId)
}
