package com.shortvideo.app.core.push

/**
 * 本地通知 ID 常量，避免业务侧魔法数字冲突。
 *
 * 厂商「通知消息」由系统展示，无需本 ID；「透传 / data」需自行 [com.kit.push.builder.notifyPush] 时使用。
 */
object PushNotificationIds {
    /** 前台收到的透传 / data 推送落栏。 */
    const val DATA_MESSAGE = 3001

    /** 通用本地提醒。 */
    const val LOCAL_GENERAL = 3002
}
