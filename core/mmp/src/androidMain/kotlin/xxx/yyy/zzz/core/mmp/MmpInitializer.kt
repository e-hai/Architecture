package xxx.yyy.zzz.core.mmp

import android.content.Context
import com.kit.mmp.appsflyer.AppsFlyerClient
import com.kit.mmp.config.MmpConfig
import com.kit.mmp.Mmp as AndroidNativeMmp

/**
 * Android 端归因初始化器。
 */
object MmpInitializer {
    fun initialize(
        context: Context,
        debug: Boolean,
        appToken: String,
    ) {
        if (appToken.isBlank()) return

        AndroidNativeMmp.init(
            context = context,
            config =
                MmpConfig(
                    appToken = appToken,
                    isDebug = debug,
                    enableLog = debug,
                ),
            client = AppsFlyerClient(),
        )

        MmpKit.install(
            object : MmpDelegate {
                override fun trackEvent(
                    eventName: String,
                    params: Map<String, Any?>,
                ) {
                    val nonNullParams = params.filterValues { it != null }.mapValues { it.value!! }
                    AndroidNativeMmp.trackEvent(eventName, nonNullParams)
                }

                override fun setCustomerUserId(userId: String?) {
                    println("[Mmp-Android] setCustomerUserId: $userId")
                }
            },
        )
    }
}
