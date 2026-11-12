package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import com.kit.analytics.Analytics
import com.kit.analytics.AnalyticsConfig
import com.kit.analytics.firebase.FirebaseAnalyticsProvider
import com.kit.analytics.provider.LoggingAnalyticsProvider
import xxx.yyy.zzz.core.analytics.AnalyticsDelegate
import xxx.yyy.zzz.core.log.CoreLog

/**
 * Android 数据统计委托实现（1:1 透传 AnalyticsKit-Android 原生 SDK）。
 */
internal class AndroidAnalyticsDelegate(
    private val context: Context,
    private val debug: Boolean,
) : AnalyticsDelegate {
    override fun doInit() {
        runCatching {
            Analytics.initialize(
                context = context,
                config =
                    AnalyticsConfig(
                        providers =
                            buildList {
                                if (debug) {
                                    add(LoggingAnalyticsProvider())
                                }
                                add(FirebaseAnalyticsProvider())
                            },
                        enabled = true,
                        debug = debug,
                    ),
            )
        }.onFailure {
            CoreLog.w("Analytics", "Analytics init skipped or failed: ${it.message}")
        }
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        Analytics.logEvent(name) {
            params.forEach { (k, v) ->
                when (v) {
                    is String -> param(k, v)
                    is Int -> param(k, v.toLong())
                    is Long -> param(k, v)
                    is Double -> param(k, v)
                    is Float -> param(k, v.toDouble())
                    is Boolean -> param(k, v.toString())
                    else -> if (v != null) param(k, v.toString())
                }
            }
        }
    }

    override fun setUserId(userId: String?) {
        Analytics.setUserId(userId)
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        Analytics.setUserProperty(name, value)
    }
}
