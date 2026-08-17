package com.shortvideo.app.core.push

/**
 * 通知渠道 ID 常量。
 *
 * 约定：稳定、可读的 snake 风格 ID；展示名在 [PushInitializer] 创建渠道时配置。
 * 业务发通知时使用 `channelId = PushChannels.GENERAL`，禁止硬编码字符串。
 */
object PushChannels {
    /** 普通消息 / 系统提醒。 */
    const val GENERAL = "channel_general"

    /** 营销 / 运营活动（用户可在系统设置中单独关闭）。 */
    const val MARKETING = "channel_marketing"

    /** 重要提醒（较高重要性）。 */
    const val IMPORTANT = "channel_important"
}
