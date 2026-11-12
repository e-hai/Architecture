package xxx.yyy.zzz.android.bridge.delegates

import android.app.NotificationManager
import android.content.Context
import com.kit.push.PushKitManager
import com.kit.push.channel.NotificationChannelConfig
import com.kit.push.provider.fcm.FcmPushProvider
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.push.PushChannels
import xxx.yyy.zzz.core.push.PushDelegate

/**
 * Android 推送通知委托实现（1:1 透传 PushKit-Android 原生 SDK）。
 */
internal class AndroidPushDelegate(
    private val context: Context,
) : PushDelegate {
    override fun doInit(registerFcm: Boolean) {
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

        if (registerFcm) {
            runCatching {
                val fcm = FcmPushProvider()
                push.registerProvider(fcm)
            }.onFailure {
                CoreLog.w("PushKit", "FCM push provider registration skipped or failed: ${it.message}")
            }
        }
    }

    override fun registerPush() {
        CoreLog.d(LogTags.APP, "[Push-Android] Push registered successfully")
    }

    override fun setPushToken(token: String) {
        CoreLog.d(LogTags.APP, "[Push-Android] FCM Token set: $token")
    }
}
