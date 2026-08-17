package com.shortvideo.app.core.analytics

import android.content.Context
import com.kit.analytics.Analytics
import com.kit.analytics.AnalyticsConfig
import com.kit.analytics.firebase.FirebaseAnalyticsProvider
import com.kit.analytics.provider.LoggingAnalyticsProvider

/**
 * AnalyticsKit 初始化入口。
 * 集中配置 Provider，避免 Application 散落 SDK 细节。
 */
object AnalyticsInitializer {
    /**
     * 初始化统计 SDK。须在业务打点之前调用一次。
     *
     * @param context Application Context
     * @param debug 为 true 时叠加 Logcat Provider，并打开 Kit debug 日志
     */
    fun initialize(
        context: Context,
        debug: Boolean,
    ) {
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
    }
}
