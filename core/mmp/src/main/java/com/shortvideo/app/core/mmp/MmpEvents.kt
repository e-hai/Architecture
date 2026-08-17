package com.shortvideo.app.core.mmp

/**
 * MMP 事件名常量。
 *
 * 约定与 [com.shortvideo.app.core.analytics.AnalyticsEvents] 一致：snake_case、禁止硬编码。
 * Adjust 平台下事件名需对应后台 **event token**，接入时按渠道配置覆盖。
 *
 * 用法：
 * ```
 * Mmp.trackEvent(MmpEvents.LEVEL_COMPLETE, mapOf(MmpParams.LEVEL to 3))
 * ```
 */
object MmpEvents {
    /** 关卡完成。 */
    const val LEVEL_COMPLETE = "level_complete"

    /** 完成注册 / 登录成功。 */
    const val COMPLETE_REGISTRATION = "complete_registration"

    /** 发起结账。 */
    const val INITIATE_CHECKOUT = "initiate_checkout"

    /** 完成购买（收入请优先用 [com.kit.mmp.Mmp.trackRevenue]）。 */
    const val PURCHASE = "purchase"

    /** 观看激励广告完成。 */
    const val AD_REWARD = "ad_reward"
}
