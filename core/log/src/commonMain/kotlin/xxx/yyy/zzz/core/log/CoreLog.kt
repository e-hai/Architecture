package xxx.yyy.zzz.core.log

/**
 * 跨平台日志委托协议（涵盖初始化、全级别日志输出、格式化日志与原生日志 UI 页面唤起）。
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

    /**
     * 仅初始化磁盘持久化日志。
     */
    fun doInitOnlyDiskLog()

    /**
     * 详细跟踪日志 (Verbose)。
     */
    fun v(
        tag: String,
        message: String,
    )

    /**
     * 调试日志 (Debug)。
     */
    fun d(
        tag: String,
        message: String,
    )

    /**
     * 普通提示日志 (Info)。
     */
    fun i(
        tag: String,
        message: String,
    )

    /**
     * 警告日志 (Warn)。
     */
    fun w(
        tag: String,
        message: String,
    )

    /**
     * 错误日志 (Error)。
     */
    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    /**
     * 严重/断言日志 (What a Terrible Failure / Assert)。
     */
    fun wtf(
        tag: String,
        message: String,
    )

    /**
     * 结构化格式化输出 JSON 字符串。
     */
    fun json(
        tag: String,
        json: String,
    )

    /**
     * 结构化格式化输出 XML 字符串。
     */
    fun xml(
        tag: String,
        xml: String,
    )

    /**
     * 异步读取磁盘持久化的历史日志记录。
     *
     * @param callback 回调返回日志文本行列表
     */
    fun readLog(callback: (List<String>) -> Unit)

    /**
     * 唤起平台原生内置的 LogKit 全屏日志查看器控制台 UI。
     * 支持 Tag 搜索与过滤、级别筛选、主题切换及日志复制。
     */
    fun showLogUi()
}

/**
 * 默认控制台兜底实现。
 */
object DefaultLogDelegate : LogDelegate {
    override fun doInitAllLog() = Unit

    override fun doInitOnlyConsoleLog() = Unit

    override fun doInitOnlyDiskLog() = Unit

    override fun v(
        tag: String,
        message: String,
    ) = println("[$tag][VERBOSE] $message")

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

    override fun wtf(
        tag: String,
        message: String,
    ) = println("[$tag][WTF] $message")

    override fun json(
        tag: String,
        json: String,
    ) = println("[$tag][JSON] $json")

    override fun xml(
        tag: String,
        xml: String,
    ) = println("[$tag][XML] $xml")

    override fun readLog(callback: (List<String>) -> Unit) {
        callback(emptyList())
    }

    override fun showLogUi() {
        println("[DefaultLogDelegate] showLogUi invoked (no-op in fallback delegate)")
    }
}

/**
 * 跨平台静态日志门面（CoreLog），与底层原生 SDK 完全解耦。
 * 外部（包括业务层、UI 层及宿主层）所有日志初始化、输出与原生日志控制台均统一通过此门面调用。
 */
object CoreLog : LogDelegate {
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

    override fun doInitOnlyDiskLog() = delegate.doInitOnlyDiskLog()

    override fun v(
        tag: String,
        message: String,
    ) = delegate.v(tag, message)

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

    override fun wtf(
        tag: String,
        message: String,
    ) = delegate.wtf(tag, message)

    override fun json(
        tag: String,
        json: String,
    ) = delegate.json(tag, json)

    override fun xml(
        tag: String,
        xml: String,
    ) = delegate.xml(tag, xml)

    override fun readLog(callback: (List<String>) -> Unit) = delegate.readLog(callback)

    override fun showLogUi() = delegate.showLogUi()
}
