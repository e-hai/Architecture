package xxx.yyy.zzz.core.mmp

/**
 * 内存可编程 MMP 归因模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeMmpDelegate : MmpDelegate {
    data class MmpEvent(
        val name: String,
        val params: Map<String, Any?>,
    )

    var initializedToken: String? = null
    var recordedCustomerUserId: String? = null
    val trackedEvents = mutableListOf<MmpEvent>()

    override fun doInit(appToken: String) {
        initializedToken = appToken
    }

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        trackedEvents.add(MmpEvent(eventName, params))
    }

    override fun setCustomerUserId(userId: String?) {
        recordedCustomerUserId = userId
    }

    fun clear() {
        initializedToken = null
        recordedCustomerUserId = null
        trackedEvents.clear()
    }
}
