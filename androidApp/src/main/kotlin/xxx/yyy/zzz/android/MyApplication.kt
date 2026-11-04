package xxx.yyy.zzz.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import xxx.yyy.zzz.core.abtesting.AbTestingInitializer
import xxx.yyy.zzz.core.ads.AdsInitializer
import xxx.yyy.zzz.core.analytics.AnalyticsInitializer
import xxx.yyy.zzz.core.crashreport.CrashReportInitializer
import xxx.yyy.zzz.core.log.LogInitializer
import xxx.yyy.zzz.core.mmp.MmpInitializer
import xxx.yyy.zzz.core.pay.PayInitializer
import xxx.yyy.zzz.core.push.PushInitializer
import xxx.yyy.zzz.shared.di.AppBootstrap

/**
 * Android 独立宿主 Application 入口。
 * 负责平台级初始化：初始化 8 大商业化原生 Kit 及启动 Koin 依赖注入。
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. 初始化跨平台全局 Koin 容器并注入 Android Context
        AppBootstrap.doInitKoin {
            androidContext(this@MyApplication)
        }

        // 2. 初始化底层 8 大商业化与基础设施原生 Kit
        LogInitializer.initialize(this, withDisk = true)
        CrashReportInitializer.initialize(this, debug = true)
        AnalyticsInitializer.initialize(this, debug = true)
        AbTestingInitializer.initialize(this, debug = true)
        MmpInitializer.initialize(this, debug = true, appToken = "dummy_appsflyer_key")
        AdsInitializer.initialize(this, apiKey = "ca-app-pub-3940256099942544~3347511713")
        PayInitializer.configure(this)
        PushInitializer.initialize(this, registerFcm = false)
    }
}
