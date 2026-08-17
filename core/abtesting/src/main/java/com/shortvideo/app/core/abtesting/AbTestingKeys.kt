package com.shortvideo.app.core.abtesting

/**
 * Remote Config / A/B 参数键与本地 defaults。
 *
 * 约定：业务用 `AbTestingClient.getBoolean(AbTestingKeys.FEATURE_X, false)`，禁止硬编码 key。
 * [defaults] 供启动时 `setDefaults`，远端未就绪也能读到合理值。
 */
object AbTestingKeys {
    /** 示例：某功能开关。 */
    const val FEATURE_X = "feature_x"

    /** 示例：欢迎文案。 */
    const val WELCOME_MESSAGE = "welcome_message"

    /** 示例：列表最大条数。 */
    const val MAX_ITEMS = "max_items"

    /** 示例：分数阈值。 */
    const val SCORE_THRESHOLD = "score_threshold"

    /**
     * 本地兜底默认值（非远端配置本身）。
     * 真实项目按产品表维护，并与 Firebase 控制台 key 对齐。
     */
    fun defaults(): Map<String, Any> =
        mapOf(
            FEATURE_X to false,
            WELCOME_MESSAGE to "Hello",
            MAX_ITEMS to 10L,
            SCORE_THRESHOLD to 0.75,
        )
}
