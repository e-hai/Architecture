package xxx.yyy.zzz.core.analytics

/**
 * 统计事件参数键常量。
 *
 * 约定：
 * - 键名 snake_case，与事件名风格一致
 * - 取值由调用方传入；本 object 只定义键，不定义业务枚举值
 * - 屏幕浏览优先使用 `Analytics.logScreenView(...)`，不必手写 screen_view 事件
 */
object AnalyticsParams {
    /** 屏幕名称（自定义曝光或补充字段时使用）。 */
    const val SCREEN_NAME = "screen_name"

    /** 屏幕对应的类名 / Composable 标识。 */
    const val SCREEN_CLASS = "screen_class"

    /** 按钮标识。 */
    const val BUTTON_ID = "button_id"

    /** 列表项 / 内容 ID。 */
    const val ITEM_ID = "item_id"

    /** 内容类型（如 article、product）。 */
    const val ITEM_TYPE = "item_type"

    /** 来源（如 push、deeplink、organic）。 */
    const val SOURCE = "source"

    /** 错误码或失败原因（短字符串）。 */
    const val ERROR_CODE = "error_code"
}
