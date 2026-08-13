package xxx.yyy.zzz

import android.app.Application
import com.kit.log.LogKit
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import xxx.yyy.zzz.core.abtesting.AbTestingInitializer
import xxx.yyy.zzz.core.ads.AdsInitializer
import xxx.yyy.zzz.core.ads.AdsKeys
import xxx.yyy.zzz.core.analytics.AnalyticsInitializer
import xxx.yyy.zzz.core.crashreport.CrashReportInitializer
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.log.LogInitializer
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.MmpInitializer
import xxx.yyy.zzz.core.network.coreNetworkModule
import xxx.yyy.zzz.core.pay.PayInitializer
import xxx.yyy.zzz.core.push.PushInitializer

/**
 * Application 入口：初始化 Log / CrashReport / Analytics / AbTesting / MMP / Ads / Pay / Push 与 Koin 模块。
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        LogInitializer.initialize(
            context = this,
            withDisk = BuildConfig.DEBUG,
        )

        CrashReportInitializer.initialize(
            context = this,
            debug = BuildConfig.DEBUG,
        )

        AnalyticsInitializer.initialize(
            context = this,
            debug = BuildConfig.DEBUG,
        )

        AbTestingInitializer.initialize(
            context = this,
            debug = BuildConfig.DEBUG,
        )

        MmpInitializer.initialize(
            context = this,
            debug = BuildConfig.DEBUG,
            appToken = BuildConfig.MMP_APP_TOKEN,
        )

        AdsInitializer.initialize(
            context = this,
            apiKey = BuildConfig.ADMOB_APP_ID.ifBlank { AdsKeys.AdMobTest.APP_ID },
        )

        PayInitializer.configure(this)

        PushInitializer.initialize(
            context = this,
            registerFcm = true,
            tokenListener = { token, provider ->
                LogKit.d(LogTags.PUSH, "token provider=$provider token=$token")
            },
        )

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                coreNetworkModule,
                coreDatabaseModule,
                coreDatastoreModule,
            )
        }
    }
}
