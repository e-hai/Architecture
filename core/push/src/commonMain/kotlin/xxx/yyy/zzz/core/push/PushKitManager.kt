package xxx.yyy.zzz.core.push

/**
 * 跨平台推送委托协议。
 */
interface PushDelegate {
    fun registerPush()

    fun setPushToken(token: String)
}

/**
 * 默认推送兜底实现。
 */
object DefaultPushDelegate : PushDelegate {
    override fun registerPush() {
        println("[Push] registerPush called (NoOp)")
    }

    override fun setPushToken(token: String) {
        println("[Push] setPushToken: $token")
    }
}

/**
 * 跨平台静态推送门面。
 */
object PushKitManager : PushDelegate {
    private var delegate: PushDelegate = DefaultPushDelegate

    fun install(platformDelegate: PushDelegate) {
        delegate = platformDelegate
    }

    override fun registerPush() = delegate.registerPush()

    override fun setPushToken(token: String) = delegate.setPushToken(token)
}
