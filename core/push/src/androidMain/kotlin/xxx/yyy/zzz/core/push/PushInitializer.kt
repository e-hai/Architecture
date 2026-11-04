package xxx.yyy.zzz.core.push

import android.app.NotificationManager
import android.content.Context
import com.kit.push.channel.NotificationChannelConfig
import com.kit.push.provider.fcm.FcmPushProvider
import com.kit.push.PushKitManager as AndroidNativePushKitManager

/**
 * Android 端推送初始化器。
 */
object PushInitializer {
    fun initialize(
        context: Context,
        registerFcm: Boolean = true,
    ) {
        val push = AndroidNativePushKitManager.init(context)

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

        if (registerFcm) {
            val fcm = FcmPushProvider()
            push.registerProvider(fcm)
        }

        PushKitManager.install(
            object : PushDelegate {
                override fun registerPush() {
                    println("[Push-Android] Push initialized successfully")
                }

                override fun setPushToken(token: String) {
                    println("[Push-Android] FCM Token set: $token")
                }
            },
        )
    }
}
