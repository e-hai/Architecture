package xxx.yyy.zzz.core.analytics

/**
 * 统计事件名常量。
 */
object AnalyticsEvents {
    object App {
        const val COLD_START = "app_cold_start"
        const val FOREGROUND = "app_foreground"
        const val BACKGROUND = "app_background"
    }

    object Common {
        const val BUTTON_CLICK = "button_click"
        const val ITEM_CLICK = "item_click"
        const val IMPRESSION = "impression"
    }
}
