package xxx.yyy.zzz.core.crashreport

/**
 * 跨平台崩溃采集委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 CrashReportKit SDK。
 */
interface CrashReportDelegate {
    /**
     * 初始化崩溃采集 SDK。
     */
    fun doInit()

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
    override fun doInit() = Unit

    override fun setUserId(userId: String) = Unit

    override fun setCustomKey(
        key: String,
        value: Any,
    ) = Unit

    override fun log(message: String) = Unit

    override fun recordException(throwable: Throwable) = Unit
}

/**
 * 跨平台崩溃上报静态门面（CoreCrashReport）。
 */
object CoreCrashReport : CrashReportDelegate {
    private var delegate: CrashReportDelegate = DefaultCrashReportDelegate

    /**
     * 挂载平台特定的崩溃采集委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的崩溃采集委托接口
     */
    fun install(delegate: CrashReportDelegate) {
        this.delegate = delegate
    }

    override fun doInit() = delegate.doInit()

    override fun setUserId(userId: String) = delegate.setUserId(userId)

    override fun setCustomKey(
        key: String,
        value: Any,
    ) = delegate.setCustomKey(key, value)

    override fun log(message: String) = delegate.log(message)

    override fun recordException(throwable: Throwable) = delegate.recordException(throwable)
}
