@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.android.bridge

import android.app.Application
// [KIT_IMPORTS_START]
import io.github.pagkit.PagKitAndroid
import xxx.yyy.zzz.android.bridge.delegates.AndroidAbTestingDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidAdsDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidAnalyticsDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidCrashReportDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidLogDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidMmpDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidPayDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidPushDelegate
import xxx.yyy.zzz.android.bridge.delegates.AndroidVideoDelegate
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.ads.CoreAds
import xxx.yyy.zzz.core.analytics.CoreAnalytics
import xxx.yyy.zzz.core.crashreport.CoreCrashReport
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.pag.CorePag
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.push.CorePush
import xxx.yyy.zzz.core.video.CoreVideo
// [KIT_IMPORTS_END]

/**
 * Android 原生商业化与基础设施 Kit 引导器（NativeKitBootstrap）。
 * 专职负责十大 Kit 的代理装配与跨平台门面注入（委托实现均独立拆分在 delegates 目录），
 * 与 iOS 端的 NativeKitBootstrap 保持 1:1 对称与相同设计范式。
 */
object NativeKitBootstrap {
    /**
     * 批量初始化所有 Android 原生 Kit 并绑定至 Core 跨平台门面。
     *
     * @param application Android Application 实例
     * @param debug 是否开启调试模式
     * @param admobAppId AdMob 应用 ID (来自 BuildConfig.ADMOB_APP_ID)
     * @param admobRewardedId AdMob 激励视频 ID (来自 BuildConfig.ADMOB_REWARDED_ID)
     * @param appsFlyerKey AppsFlyer Token (来自 BuildConfig.APPSFLYER_KEY)
     */
    fun initKits(
        application: Application,
        debug: Boolean = true,
        admobAppId: String = "",
        admobRewardedId: String = "",
        appsFlyerKey: String = "",
    ) {
        // [KIT_INSTALL_START]
        // [KIT:log:START]
        CoreLog.install(AndroidLogDelegate(application))
        CoreLog.doInitAllLog()
        // [KIT:log:END]

        // [KIT:crashreport:START]
        CoreCrashReport.install(AndroidCrashReportDelegate(application, debug))
        CoreCrashReport.doInit()
        // [KIT:crashreport:END]

        // [KIT:analytics:START]
        CoreAnalytics.install(AndroidAnalyticsDelegate(application, debug))
        CoreAnalytics.doInit()
        // [KIT:analytics:END]

        // [KIT:abtesting:START]
        CoreAbTesting.install(AndroidAbTestingDelegate(application, debug))
        CoreAbTesting.doInit()
        // [KIT:abtesting:END]

        // [KIT:mmp:START]
        CoreMmp.install(AndroidMmpDelegate(application, debug))
        CoreMmp.doInit(appToken = appsFlyerKey)
        // [KIT:mmp:END]

        // [KIT:ads:START]
        CoreAds.install(
            AndroidAdsDelegate(
                application = application,
                defaultRewardedUnitId = admobRewardedId,
                debug = debug,
            ),
        )
        CoreAds.doInit(apiKey = admobAppId)
        // [KIT:ads:END]

        // [KIT:pay:START]
        CorePay.install(AndroidPayDelegate(application))
        CorePay.doInit()
        // [KIT:pay:END]

        // [KIT:push:START]
        CorePush.install(AndroidPushDelegate(application))
        CorePush.doInit(registerFcm = false)
        // [KIT:push:END]

        // [KIT:video:START]
        CoreVideo.install(AndroidVideoDelegate(application))
        CoreVideo.doInit()
        // [KIT:video:END]

        // [KIT:pag:START]
        PagKitAndroid.init(application)
        CorePag.doInit()
        // [KIT:pag:END]
        // [KIT_INSTALL_END]

        CoreLog.i(LogTags.APP, "Android native kits initialized successfully.")
    }
}
