package xxx.yyy.zzz.core.analytics

import android.content.Context
import com.kit.analytics.AnalyticsConfig
import com.kit.analytics.firebase.FirebaseAnalyticsProvider
import com.kit.analytics.provider.LoggingAnalyticsProvider
import com.kit.analytics.Analytics as AndroidNativeAnalytics

/**
 * Android 端统计初始化器。
 */
object AnalyticsInitializer {
    fun initialize(
        context: Context,
        debug: Boolean,
    ) {
        AndroidNativeAnalytics.initialize(
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

        Analytics.install(
            object : AnalyticsDelegate {
                override fun logEvent(
                    name: String,
                    params: Map<String, Any?>,
                ) {
                    AndroidNativeAnalytics.logEvent(name) {
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
                    AndroidNativeAnalytics.setUserId(userId)
                }

                override fun setUserProperty(
                    name: String,
                    value: String?,
                ) {
                    AndroidNativeAnalytics.setUserProperty(name, value)
                }
            },
        )
    }
}
