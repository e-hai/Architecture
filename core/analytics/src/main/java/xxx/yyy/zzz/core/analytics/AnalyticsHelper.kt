package xxx.yyy.zzz.core.analytics

/**
 * 数据分析助手接口
 * 屏蔽具体统计 SDK 的实现细节，提供统一的事件追踪接口
 */
interface AnalyticsHelper {
    /**
     * 记录自定义事件
     * @param event 分析事件对象
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * 设置用户属性
     * @param name 属性名称
     * @param value 属性值
     */
    fun setUserProperty(name: String, value: String?)

    /**
     * 设置用户 ID
     * @param userId 用户 ID
     */
    fun setUserId(userId: String?)

    /**
     * 启用/禁用分析收集
     * @param enabled 是否启用
     */
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
