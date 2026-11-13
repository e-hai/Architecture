package xxx.yyy.zzz.android.bridge.delegates

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import com.kit.push.PushKitManager
import com.kit.push.channel.NotificationChannelConfig
import com.kit.push.permission.NotificationPermissionHelper
import com.kit.push.provider.PushMessageListener
import com.kit.push.provider.PushTokenListener
import com.kit.push.provider.fcm.FcmPushProvider
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.push.PushChannels
import xxx.yyy.zzz.core.push.PushDelegate
import java.lang.ref.WeakReference

/**
 * Android 宿主 Activity 通知权限与推送点击 Intent 桥接器。
 * 由 `MainActivity` 在生命周期内挂载与清理，避免 Delegate 强持有 Activity 造成内存泄漏。
 */
internal object AndroidPushBridge {
    private var launcherRef: WeakReference<ActivityResultLauncher<String>>? = null
    private var permissionCallback: ((Boolean) -> Unit)? = null

    fun bindPermissionLauncher(
        launcher: ActivityResultLauncher<String>,
        onResult: ((Boolean) -> Unit)? = null,
    ) {
        launcherRef = WeakReference(launcher)
        if (onResult != null) {
            permissionCallback = onResult
        }
    }

    fun clearPermissionLauncher() {
        launcherRef = null
    }

    fun onPermissionResult(granted: Boolean) {
        CoreLog.i(LogTags.PUSH, "[Push-Android] POST_NOTIFICATIONS 权限申请结果: granted=$granted")
        permissionCallback?.invoke(granted)
    }

    fun requestNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val launcher = launcherRef?.get() ?: return false
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return true
    }

    fun handleNotificationIntent(intent: Intent?) {
        val extras = intent?.extras ?: return
        val keys = extras.keySet().orEmpty()
        if (keys.isEmpty()) return
        val hasPushPayload = keys.any { it.startsWith("google.") || it == "from" || it == "push_route" }
        if (hasPushPayload) {
            CoreLog.i(LogTags.PUSH, "[Push-Android] 捕获通知点击启动/回前台载荷 keys=$keys")
        }
    }
}

/**
 * Android 推送通知委托实现（1:1 透传 PushKit-Android 原生 SDK）。
 */
internal class AndroidPushDelegate(
    private val context: Context,
) : PushDelegate {
    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var fcmProvider: FcmPushProvider? = null

    @Volatile
    private var fcmEnabled: Boolean = false

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

        fcmEnabled = registerFcm
        if (registerFcm) {
            runCatching {
                val fcm = FcmPushProvider()
                fcm.setMessageListener(
                    PushMessageListener { message, provider ->
                        CoreLog.i(
                            LogTags.PUSH,
                            "[Push-Android] 收到推送消息 ($provider): id=${message.id}, title=${message.title}",
                        )
                    },
                )
                push.registerProvider(fcm)
                fcmProvider = fcm
            }.onFailure {
                CoreLog.w(LogTags.PUSH, "FCM push provider registration skipped or failed: ${it.message}")
            }
        }
    }

    override fun registerPush() {
        val granted = NotificationPermissionHelper.hasNotificationPermission(context)
        if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launched = AndroidPushBridge.requestNotificationPermission()
            if (!launched) {
                CoreLog.w(LogTags.PUSH, "[Push-Android] 当前无可用前台 Activity 权限请求器，跳过 POST_NOTIFICATIONS 弹窗")
            }
        }

        val provider = fcmProvider
        if (fcmEnabled && provider != null) {
            runCatching {
                provider.register(
                    context,
                    PushTokenListener { token, providerName ->
                        cachedToken = token
                        CoreLog.i(
                            LogTags.PUSH,
                            "[Push-Android] 收到 $providerName Token (len=${token.length})",
                        )
                    },
                )
            }.onFailure {
                CoreLog.w(LogTags.PUSH, "[Push-Android] FCM register failed: ${it.message}")
            }
        } else {
            CoreLog.d(
                LogTags.PUSH,
                "[Push-Android] 推送注册完成 (hasPermission=$granted, fcmEnabled=$fcmEnabled)",
            )
        }
    }

    override fun setPushToken(token: String) {
        cachedToken = token
        CoreLog.d(LogTags.PUSH, "[Push-Android] Push Token updated (len=${token.length})")
    }

    override fun getPushToken(): String? = cachedToken ?: runCatching { fcmProvider?.getToken(context) }.getOrNull()

    override fun hasPushPermission(): Boolean = NotificationPermissionHelper.hasNotificationPermission(context)
}
