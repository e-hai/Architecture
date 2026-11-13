package xxx.yyy.zzz.core.push

/**
 * 内存可编程推送通知模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakePushDelegate : PushDelegate {
    var initializedWithFcm: Boolean? = null
    var registered = false
    var currentToken: String? = null
    var permissionGranted = false

    override fun doInit(registerFcm: Boolean) {
        initializedWithFcm = registerFcm
    }

    override fun registerPush() {
        registered = true
    }

    override fun setPushToken(token: String) {
        currentToken = token
    }

    override fun getPushToken(): String? = currentToken

    override fun hasPushPermission(): Boolean = permissionGranted

    fun clear() {
        initializedWithFcm = null
        registered = false
        currentToken = null
        permissionGranted = false
    }
}
