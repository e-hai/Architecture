package xxx.yyy.zzz.core.log

/**
 * 跨平台日志委托协议。
 */
interface LogDelegate {
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
 */
object LogKit : LogDelegate {
    private var delegate: LogDelegate = DefaultLogDelegate

    fun install(platformDelegate: LogDelegate) {
        delegate = platformDelegate
    }

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
