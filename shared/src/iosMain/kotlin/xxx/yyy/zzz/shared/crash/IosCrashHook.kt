@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared.crash

import kotlin.experimental.ExperimentalNativeApi
// [KIT:crashreport:START]
import xxx.yyy.zzz.core.crashreport.CoreCrashReport
// [KIT:crashreport:END]

/**
 * iOS 平台 Kotlin/Native 未捕获异常钩子（IosCrashHook）。
 *
 * 在宿主冷启动 Stage 1 注册系统级 `setUnhandledExceptionHook`：
 * 1. 当跨端 Kotlin 代码发生未捕获异常并即将终止进程时，先提取原始 Kotlin 异常类名与完整调用栈；
 * 2. 通过 [CoreCrashReport] 写入自定义键、日志面包屑并记录异常信息，避免 Crashlytics 仅剩 `konan::abort` 无效栈；
 * 3. 上报动作受 `runCatching` 保护，无论上报是否成功均继续执行原钩子或 `terminateWithUnhandledException` 以遵循系统崩溃语义。
 */
@OptIn(ExperimentalNativeApi::class)
object IosCrashHook {
    private const val MAX_STACK_TRACE_LENGTH = 4096
    private const val KEY_KOTLIN_EXCEPTION_TYPE = "kotlin_exception_type"

    private var isInstalled = false
    private var previousHook: ((Throwable) -> Unit)? = null

    /**
     * 幂等安装 Kotlin/Native 全局未捕获异常捕获钩子。
     */
    fun doInstall() {
        if (isInstalled) return
        isInstalled = true
        val installedPrevious =
            setUnhandledExceptionHook { throwable ->
                handleUnhandledException(throwable) { err ->
                    val prev = previousHook
                    if (prev != null) {
                        prev.invoke(err)
                    } else {
                        terminateWithUnhandledException(err)
                    }
                }
            }
        previousHook = installedPrevious
    }

    /**
     * 提取截断后的 Kotlin 异常完整堆栈字符串，供 Swift `IosCrashReportDelegate` 补充面包屑与 `NSError` 信息。
     *
     * @param throwable Kotlin 异常实例
     * @return 截断在 [MAX_STACK_TRACE_LENGTH] 以内的可读调用栈
     */
    fun formatStackTrace(throwable: Throwable): String = throwable.stackTraceToString().take(MAX_STACK_TRACE_LENGTH)

    /**
     * 提取可读的 Kotlin 异常类型全限定名或简名。
     *
     * @param throwable Kotlin 异常实例
     * @return 异常类型名称
     */
    fun exceptionTypeName(throwable: Throwable): String = throwable::class.qualifiedName ?: throwable::class.simpleName ?: "Throwable"

    internal fun handleUnhandledException(
        throwable: Throwable,
        onTerminate: (Throwable) -> Unit,
    ) {
        // [KIT:crashreport:START]
        runCatching {
            val typeName = exceptionTypeName(throwable)
            val stackTrace = formatStackTrace(throwable)
            CoreCrashReport.setCustomKey(KEY_KOTLIN_EXCEPTION_TYPE, typeName)
            CoreCrashReport.log("[K/N UnhandledException] $stackTrace")
            CoreCrashReport.recordException(throwable)
        }
        // [KIT:crashreport:END]
        onTerminate(throwable)
    }

    internal fun resetForTesting() {
        isInstalled = false
        previousHook = null
    }
}
