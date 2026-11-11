package xxx.yyy.zzz.core.log

/**
 * 专供单元测试拦截断言的 FakeLogDelegate 实现（遵循 Fake over Mock 原则）。
 */
class FakeLogDelegate : LogDelegate {
    data class LogEntry(
        val level: String,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null,
    )

    val logs = mutableListOf<LogEntry>()
    var showLogUiCalledCount = 0
    var readLogResult: List<String> = emptyList()

    override fun doInitAllLog() = Unit

    override fun doInitOnlyConsoleLog() = Unit

    override fun doInitOnlyDiskLog() = Unit

    override fun v(
        tag: String,
        message: String,
    ) {
        logs.add(LogEntry("VERBOSE", tag, message))
    }

    override fun d(
        tag: String,
        message: String,
    ) {
        logs.add(LogEntry("DEBUG", tag, message))
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        logs.add(LogEntry("INFO", tag, message))
    }

    override fun w(
        tag: String,
        message: String,
    ) {
        logs.add(LogEntry("WARN", tag, message))
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        logs.add(LogEntry("ERROR", tag, message, throwable))
    }

    override fun wtf(
        tag: String,
        message: String,
    ) {
        logs.add(LogEntry("WTF", tag, message))
    }

    override fun json(
        tag: String,
        json: String,
    ) {
        logs.add(LogEntry("JSON", tag, json))
    }

    override fun xml(
        tag: String,
        xml: String,
    ) {
        logs.add(LogEntry("XML", tag, xml))
    }

    override fun readLog(callback: (List<String>) -> Unit) {
        callback(readLogResult)
    }

    override fun showLogUi() {
        showLogUiCalledCount++
    }

    fun clear() {
        logs.clear()
        showLogUiCalledCount = 0
    }
}
