package xxx.yyy.zzz.core.abtesting

/**
 * A/B 实验与远程配置键名常量。
 */
object AbTestingKeys {
    const val FEATURE_X = "feature_x"
    const val WELCOME_MESSAGE = "welcome_message"
    const val MAX_ITEMS = "max_items"
    const val SCORE_THRESHOLD = "score_threshold"

    fun defaults(): Map<String, Any> =
        mapOf(
            FEATURE_X to false,
            WELCOME_MESSAGE to "欢迎体验 KMP 现代化架构！",
            MAX_ITEMS to 10L,
            SCORE_THRESHOLD to 0.75,
        )
}
