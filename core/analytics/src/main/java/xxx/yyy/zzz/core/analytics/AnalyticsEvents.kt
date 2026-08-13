package xxx.yyy.zzz.core.analytics

/**
 * 统计事件名常量。
 *
 * 约定：
 * - 全部小写 + 下划线（snake_case），与 Firebase 推荐一致
 * - 按业务域分子 object，禁止在业务代码中硬编码事件字符串
 * - Feature 专属事件可在对应 feature 模块定义，跨模块共用的放本文件
 *
 * 用法示例：
 * ```
 * Analytics.logEvent(AnalyticsEvents.Common.BUTTON_CLICK) {
 *     param(AnalyticsParams.BUTTON_ID, "checkout")
 * }
 * ```
 */
object AnalyticsEvents {
    /** 应用生命周期相关事件。 */
    object App {
        /** 冷启动完成（Application / 首屏可展示）。 */
        const val COLD_START = "app_cold_start"

        /** 从后台回到前台。 */
        const val FOREGROUND = "app_foreground"

        /** 进入后台。 */
        const val BACKGROUND = "app_background"
    }

    /** 跨业务通用交互事件。 */
    object Common {
        /** 按钮点击。参数建议带 [AnalyticsParams.BUTTON_ID]。 */
        const val BUTTON_CLICK = "button_click"

        /** 列表项点击。参数建议带 [AnalyticsParams.ITEM_ID]。 */
        const val ITEM_CLICK = "item_click"

        /** 页面/内容曝光。参数建议带 [AnalyticsParams.ITEM_ID] 或 [AnalyticsParams.SCREEN_NAME]。 */
        const val IMPRESSION = "impression"
    }
}
