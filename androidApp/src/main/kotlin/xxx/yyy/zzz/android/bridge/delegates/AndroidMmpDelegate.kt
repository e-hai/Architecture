package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import com.kit.mmp.Mmp
import com.kit.mmp.appsflyer.AppsFlyerClient
import com.kit.mmp.config.MmpConfig
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.MmpDelegate

/**
 * Android 归因统计委托实现（1:1 透传 MmpKit-Android 原生 SDK）。
 */
internal class AndroidMmpDelegate(
    private val context: Context,
    private val debug: Boolean,
) : MmpDelegate {
    override fun doInit(appToken: String) {
        if (appToken.isBlank()) return
        Mmp.init(
            context = context,
            config =
                MmpConfig(
                    appToken = appToken,
                    isDebug = debug,
                    enableLog = debug,
                ),
            client = AppsFlyerClient(),
        )
    }

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        val nonNullParams = params.filterValues { it != null }.mapValues { it.value!! }
        Mmp.trackEvent(eventName, nonNullParams)
    }

    override fun setCustomerUserId(userId: String?) {
        CoreLog.d(LogTags.APP, "[Mmp-Android] setCustomerUserId: $userId")
    }
}
