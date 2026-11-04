package xxx.yyy.zzz.core.crashreport

/**
 * 跨平台崩溃采集委托协议。
 */
interface CrashReportDelegate {
    fun setUserId(userId: String)

    fun setCustomKey(
        key: String,
        value: Any,
    )

    fun log(message: String)

    fun recordException(throwable: Throwable)
}

/**
 * 默认空实现兜底。
 */
object DefaultCrashReportDelegate : CrashReportDelegate {
    override fun setUserId(userId: String) = Unit

    override fun setCustomKey(
        key: String,
        value: Any,
    ) = Unit

    override fun log(message: String) = Unit

    override fun recordException(throwable: Throwable) = Unit
}

/**
 * 跨平台崩溃上报静态门面。
 */
object CrashReport : CrashReportDelegate {
    private var delegate: CrashReportDelegate = DefaultCrashReportDelegate

    fun install(platformDelegate: CrashReportDelegate) {
        delegate = platformDelegate
    }

    override fun setUserId(userId: String) = delegate.setUserId(userId)

    override fun setCustomKey(
        key: String,
        value: Any,
    ) = delegate.setCustomKey(key, value)

    override fun log(message: String) = delegate.log(message)

    override fun recordException(throwable: Throwable) = delegate.recordException(throwable)
}
