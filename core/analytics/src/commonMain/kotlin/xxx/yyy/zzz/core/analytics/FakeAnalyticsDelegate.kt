package xxx.yyy.zzz.core.analytics

/**
 * 内存可编程数据埋点模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeAnalyticsDelegate : AnalyticsDelegate {
    data class TrackedEvent(
        val name: String,
        val params: Map<String, Any?>,
    )

    var initCount = 0
    var currentUserId: String? = null
    val userProperties = mutableMapOf<String, String?>()
    val events = mutableListOf<TrackedEvent>()

    override fun doInit() {
        initCount++
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        events.add(TrackedEvent(name, params))
    }

    override fun setUserId(userId: String?) {
        currentUserId = userId
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        userProperties[name] = value
    }

    fun clear() {
        initCount = 0
        currentUserId = null
        userProperties.clear()
        events.clear()
    }
}
