package com.shortvideo.app

import android.app.Application
import com.kit.log.LogKit
import com.shortvideo.app.core.abtesting.AbTestingInitializer
import com.shortvideo.app.core.ads.AdsInitializer
import com.shortvideo.app.core.ads.AdsKeys
import com.shortvideo.app.core.analytics.AnalyticsInitializer
import com.shortvideo.app.core.crashreport.CrashReportInitializer
import com.shortvideo.app.core.database.coreDatabaseModule
import com.shortvideo.app.core.datastore.coreDatastoreModule
import com.shortvideo.app.core.log.LogInitializer
import com.shortvideo.app.core.log.LogTags
import com.shortvideo.app.core.mmp.MmpInitializer
import com.shortvideo.app.core.network.coreNetworkModule
import com.shortvideo.app.core.pay.PayInitializer
import com.shortvideo.app.core.push.PushInitializer
import com.shortvideo.app.core.video.VideoInitializer
import com.shortvideo.app.feature.comment.impl.commentModule
import com.shortvideo.app.feature.creator.impl.creatorModule
import com.shortvideo.app.feature.discover.impl.discoverModule
import com.shortvideo.app.feature.feed.impl.feedModule
import com.shortvideo.app.feature.profile.impl.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application 入口：初始化 Log / CrashReport / Analytics / AbTesting / MMP / Ads / Pay / Push / Video 与 Koin 模块。
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

        VideoInitializer.initialize(
            context = this,
            debug = BuildConfig.DEBUG,
        )

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                coreNetworkModule,
                coreDatabaseModule,
                coreDatastoreModule,
                feedModule,
                commentModule,
                discoverModule,
                profileModule,
                creatorModule,
            )
        }
    }
}
