package xxx.yyy.zzz.core.crashreport

/**
 * 内存可编程崩溃收集模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeCrashReportDelegate : CrashReportDelegate {
    var initCount = 0
    var currentUserId: String? = null
    val customKeys = mutableMapOf<String, Any>()
    val breadcrumbs = mutableListOf<String>()
    val exceptions = mutableListOf<Throwable>()

    override fun doInit() {
        initCount++
    }

    override fun setUserId(userId: String) {
        currentUserId = userId
    }

    override fun setCustomKey(
        key: String,
        value: Any,
    ) {
        customKeys[key] = value
    }

    override fun log(message: String) {
        breadcrumbs.add(message)
    }

    override fun recordException(throwable: Throwable) {
        exceptions.add(throwable)
    }

    fun clear() {
        initCount = 0
        currentUserId = null
        customKeys.clear()
        breadcrumbs.clear()
        exceptions.clear()
    }
}
