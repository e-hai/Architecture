package com.shortvideo.app.core.mmp

import android.content.Context
import com.kit.mmp.Mmp
import com.kit.mmp.appsflyer.AppsFlyerClient
import com.kit.mmp.config.MmpConfig

/**
 * MmpKit 初始化入口。
 * 默认使用 AppsFlyer；若改用 Adjust，替换 [AppsFlyerClient] 为 AdjustClient 即可。
 */
object MmpInitializer {
    /**
     * 初始化归因 SDK。须在业务上报之前调用一次。
     *
     * @param context Application Context
     * @param debug 平台 SDK debug / sandbox，以及 MmpKit 日志
     * @param appToken AppsFlyer Dev Key（或 Adjust App Token）
     */
    fun initialize(
        context: Context,
        debug: Boolean,
        appToken: String,
    ) {
        if (appToken.isBlank()) {
            return
        }
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
}
