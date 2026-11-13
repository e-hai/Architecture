package xxx.yyy.zzz.core.push

/**
 * 跨平台推送委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 PushKit SDK。
 */
interface PushDelegate {
    /**
     * 初始化推送 SDK 与通知渠道。
     *
     * @param registerFcm 是否注册 FCM 推送服务（Android 端有效）
     */
    fun doInit(registerFcm: Boolean = false)

    fun registerPush()

    fun setPushToken(token: String)

    /**
     * 获取当前已缓存或平台侧可用的推送 Token。
     */
    fun getPushToken(): String? = null

    /**
     * 查询当前系统通知权限是否已授予。
     */
    fun hasPushPermission(): Boolean = false
}

/**
 * 默认推送兜底实现。
 */
object DefaultPushDelegate : PushDelegate {
    private var cachedToken: String? = null

    override fun doInit(registerFcm: Boolean) = Unit

    override fun registerPush() {
        println("[Push] registerPush called (NoOp)")
    }

    override fun setPushToken(token: String) {
        cachedToken = token
        println("[Push] setPushToken: $token")
    }

    override fun getPushToken(): String? = cachedToken

    override fun hasPushPermission(): Boolean = false
}

/**
 * 跨平台静态推送门面（CorePush）。
 */
object CorePush : PushDelegate {
    private var delegate: PushDelegate = DefaultPushDelegate

    /**
     * 挂载平台特定的推送委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的推送委托接口
     */
    fun install(delegate: PushDelegate) {
        this.delegate = delegate
    }

    override fun doInit(registerFcm: Boolean) = delegate.doInit(registerFcm)

    override fun registerPush() = delegate.registerPush()

    override fun setPushToken(token: String) = delegate.setPushToken(token)

    override fun getPushToken(): String? = delegate.getPushToken()

    override fun hasPushPermission(): Boolean = delegate.hasPushPermission()
}
