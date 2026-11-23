package xxx.yyy.zzz.core.abtesting

/**
 * A/B 实验与远程配置键名常量。
 */
object AbTestingKeys {
    const val FEATURE_X = "feature_x"
    const val WELCOME_MESSAGE = "welcome_message"
    const val MAX_ITEMS = "max_items"
    const val SCORE_THRESHOLD = "score_threshold"

    /** 正式环境远程配置默认最小拉取间隔（秒）。 */
    const val DEFAULT_FETCH_INTERVAL_SECONDS = 3600L

    private const val DEFAULT_MAX_ITEMS = 10L
    private const val DEFAULT_SCORE_THRESHOLD = 0.75

    fun defaults(): Map<String, Any> =
        mapOf(
            FEATURE_X to false,
            WELCOME_MESSAGE to "欢迎体验 KMP 现代化架构！",
            MAX_ITEMS to DEFAULT_MAX_ITEMS,
            SCORE_THRESHOLD to DEFAULT_SCORE_THRESHOLD,
        )
}
