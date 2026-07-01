package xxx.yyy.zzz.core.crashreport

/**
 * 崩溃上报助手接口。
 *
 * 屏蔽 Firebase Crashlytics 的具体实现细节，提供统一的
 * 崩溃记录和用户行为追踪接口。
 */
interface CrashReportHelper {

    /**
     * 记录非致命异常。
     * 适用于捕获异常但应用可继续运行的场景（例如网络请求失败后的重试）。
     * @param throwable 异常对象
     */
    fun recordException(throwable: Throwable)

    /**
     * 记录自定义日志消息。
     * 这些日志会关联到后续发生的崩溃，帮助排查问题。
     * @param message 日志内容
     */
    fun log(message: String)

    /**
     * 设置用户标识符。
     * Crashlytics 面板可选此字段筛选崩溃记录。
     * @param userId 用户唯一标识
     */
    fun setUserId(userId: String?)

    /**
     * 设置自定义键值对。
     * 每条崩溃记录会携带这些字段，便于复现和分类。
     * @param key 键
     * @param value 值
     */
    fun setCustomKey(
        key: String,
        value: String,
    )

    /**
     * 记录用户操作步骤。
     * Crashlytics 将把用户在应用内的操作路径记录为"面包屑"，
     * 发生崩溃时可回溯用户操作序列。
     * @param name 操作名称
     */
    fun logBreadcrumb(name: String)
}
