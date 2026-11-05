package xxx.yyy.zzz.core.log

/**
 * 跨平台日志委托协议（涵盖初始化与各级别日志输出）。
 * 约束：仅允许在该接口的各端实现类内部调用原生 LogKit SDK。
 */
interface LogDelegate {
    /**
     * 初始化控制台与磁盘全量日志。
     */
    fun doInitAllLog()

    /**
     * 仅初始化控制台日志。
     */
    fun doInitOnlyConsoleLog()

    fun d(
        tag: String,
        message: String,
    )

    fun i(
        tag: String,
        message: String,
    )

    fun w(
        tag: String,
        message: String,
    )

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

/**
 * 默认控制台兜底实现。
 */
object DefaultLogDelegate : LogDelegate {
    override fun doInitAllLog() = Unit

    override fun doInitOnlyConsoleLog() = Unit

    override fun d(
        tag: String,
        message: String,
    ) = println("[$tag][DEBUG] $message")

    override fun i(
        tag: String,
        message: String,
    ) = println("[$tag][INFO] $message")

    override fun w(
        tag: String,
        message: String,
    ) = println("[$tag][WARN] $message")

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        println("[$tag][ERROR] $message ${throwable?.message ?: ""}")
    }
}

/**
 * 跨平台静态日志门面，与 Android / iOS 分支体验 100% 对齐。
 * 外部（包括业务层、UI 层及宿主层）所有日志初始化与打印统一通过此门面调用。
 */
object LogKit : LogDelegate {
    private var delegate: LogDelegate = DefaultLogDelegate

    /**
     * 挂载平台特定的日志委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的日志委托接口
     */
    fun install(delegate: LogDelegate) {
        this.delegate = delegate
    }

    override fun doInitAllLog() = delegate.doInitAllLog()

    override fun doInitOnlyConsoleLog() = delegate.doInitOnlyConsoleLog()

    override fun d(
        tag: String,
        message: String,
    ) = delegate.d(tag, message)

    override fun i(
        tag: String,
        message: String,
    ) = delegate.i(tag, message)

    override fun w(
        tag: String,
        message: String,
    ) = delegate.w(tag, message)

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = delegate.e(tag, message, throwable)
}
