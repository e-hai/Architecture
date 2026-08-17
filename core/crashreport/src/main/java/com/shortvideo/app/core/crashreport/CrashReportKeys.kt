package com.shortvideo.app.core.crashreport

/**
 * 崩溃上报自定义键常量。
 *
 * 约定：
 * - 键名 snake_case
 * - 本 object 只定义键，取值由调用方传入
 */
object CrashReportKeys {
    /** 是否 Debug 构建。 */
    const val DEBUG = "debug"

    /** 构建类型（如 debug / release）。 */
    const val BUILD_TYPE = "build_type"

    /** 产品风味（如 free / paid）。 */
    const val FLAVOR = "flavor"

    /** 业务场景或页面标识。 */
    const val SCREEN = "screen"

    /** 订单 / 业务单号等可关联字段。 */
    const val ORDER_ID = "order_id"
}
