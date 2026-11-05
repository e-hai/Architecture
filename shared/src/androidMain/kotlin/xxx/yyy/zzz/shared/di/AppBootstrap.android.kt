package xxx.yyy.zzz.shared.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
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
 * Android 宿主统一初始化扩展。
 * 严格按照跨平台标准三阶段完成冷启动流程：
 * 1. 批量初始化 8 大商业化与基础设施原生 Kit 并绑定门面；
 * 2. 启动全局 Koin 依赖注入容器并注入 Android Context；
 * 3. 打印应用冷启动完成确认日志。
 *
 * @param application Android Application 实例
 * @param debug 是否开启调试模式
 */
fun AppBootstrap.init(
    application: Application,
    debug: Boolean = true,
) {
    // Phase 1: 平台级 8 大商业化 Kit 批量初始化与门面绑定
    LogInitializer.initialize(application, withDisk = true)
    CrashReportInitializer.initialize(application, debug = debug)
    AnalyticsInitializer.initialize(application, debug = debug)
    AbTestingInitializer.initialize(application, debug = debug)
    MmpInitializer.initialize(application, debug = debug, appToken = "dummy_appsflyer_key")
    AdsInitializer.initialize(application, apiKey = "ca-app-pub-3940256099942544~3347511713")
    PayInitializer.configure(application)
    PushInitializer.initialize(application, registerFcm = false)

    // Phase 2: Koin 依赖注入容器启动（注入 Android Context）
    doInitKoin {
        androidContext(application)
    }

    // Phase 3: 就绪日志
    LogKit.i(LogTags.APP, "Application bootstrap completed on Android.")
}
