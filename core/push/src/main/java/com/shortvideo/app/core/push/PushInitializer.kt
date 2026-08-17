package com.shortvideo.app.core.push

import android.app.NotificationManager
import android.content.Context
import com.kit.push.PushKitManager
import com.kit.push.channel.NotificationChannelConfig
import com.kit.push.provider.PushMessageListener
import com.kit.push.provider.PushTokenListener
import com.kit.push.provider.fcm.FcmPushProvider

/**
 * PushKit 初始化入口。
 * 创建默认通知渠道，并可选注册 FCM Provider。
 */
object PushInitializer {
    /**
     * 初始化推送与通知基础设施。
     *
     * @param context Application Context
     * @param registerFcm 是否注册 FCM（默认 true；需已配置 `google-services.json`）
     * @param tokenListener FCM Token 回调（可选）
     * @param messageListener 透传 / data 消息回调（可选；通知类消息通常由系统直接展示）
     */
    fun initialize(
        context: Context,
        registerFcm: Boolean = true,
        tokenListener: PushTokenListener? = null,
        messageListener: PushMessageListener? = null,
    ) {
        val push = PushKitManager.init(context)

        push.createChannel(
            NotificationChannelConfig(
                id = PushChannels.GENERAL,
                name = "普通通知",
                description = "日常消息与系统提醒",
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        push.createChannel(
            NotificationChannelConfig(
                id = PushChannels.MARKETING,
                name = "营销通知",
                description = "活动与运营推送",
                importance = NotificationManager.IMPORTANCE_LOW,
            ),
        )
        push.createChannel(
            NotificationChannelConfig(
                id = PushChannels.IMPORTANT,
                name = "重要通知",
                description = "需要及时关注的提醒",
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
        )

        if (!registerFcm) {
            return
        }

        val fcm =
            FcmPushProvider().apply {
                setMessageListener(messageListener)
            }
        push.registerProvider(fcm)
        // registerProvider 内部 register 不带 tokenListener，需再绑一次以接收 Token
        if (tokenListener != null) {
            fcm.register(context, tokenListener)
        }
    }
}
