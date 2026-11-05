package xxx.yyy.zzz.android.bridge

import android.app.Application
import xxx.yyy.zzz.core.abtesting.AbTestingInitializer
import xxx.yyy.zzz.core.ads.AdsInitializer
import xxx.yyy.zzz.core.analytics.AnalyticsInitializer
import xxx.yyy.zzz.core.crashreport.CrashReportInitializer
import xxx.yyy.zzz.core.log.LogInitializer
import xxx.yyy.zzz.core.log.LogKit
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.MmpInitializer
import xxx.yyy.zzz.core.pay.PayInitializer
import xxx.yyy.zzz.core.push.PushInitializer

/**
 * Android 原生商业化 Kit 引导器（NativeKitBootstrap）。
 * 专职负责未实现跨平台 KMP 的 8 大 Android 原生 AAR SDK 的批量初始化与门面挂载，
 * 与 iOS 端的 NativeKitBootstrap 保持命名与职责 100% 对齐。
 */
object NativeKitBootstrap {
    /**
     * 批量初始化所有 Android 原生 Kit 并绑定至 KMP 门面。
     *
     * @param application Android Application 实例
     * @param debug 是否开启调试模式
     */
    fun initKits(
        application: Application,
        debug: Boolean = true,
    ) {
        LogInitializer.initialize(application, withDisk = true)
        CrashReportInitializer.initialize(application, debug = debug)
        AnalyticsInitializer.initialize(application, debug = debug)
        AbTestingInitializer.initialize(application, debug = debug)
        MmpInitializer.initialize(application, debug = debug, appToken = "dummy_appsflyer_key")
        AdsInitializer.initialize(application, apiKey = "ca-app-pub-3940256099942544~3347511713")
        PayInitializer.configure(application)
        PushInitializer.initialize(application, registerFcm = false)
        LogKit.i(LogTags.APP, "Android native kits initialized successfully.")
    }
}
