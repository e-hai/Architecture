package xxx.yyy.zzz.core.analytics

/**
 * 分析事件数据类
 * 封装事件名称和参数
 * 
 * @param name 事件名称
 * @param params 事件参数键值对
 */
data class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap()
)
