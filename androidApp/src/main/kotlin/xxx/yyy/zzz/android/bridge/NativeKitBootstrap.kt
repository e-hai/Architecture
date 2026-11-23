@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.android.bridge

import android.annotation.SuppressLint
import android.app.Application
// [KIT_IMPORTS_START]
// [KIT:log:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidLogDelegate
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
// [KIT:log:END]
// [KIT:crashreport:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidCrashReportDelegate
import xxx.yyy.zzz.core.crashreport.CoreCrashReport
// [KIT:crashreport:END]
// [KIT:analytics:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidAnalyticsDelegate
import xxx.yyy.zzz.core.analytics.CoreAnalytics
// [KIT:analytics:END]
// [KIT:abtesting:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidAbTestingDelegate
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
// [KIT:abtesting:END]
// [KIT:mmp:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidMmpDelegate
import xxx.yyy.zzz.core.mmp.CoreMmp
// [KIT:mmp:END]
// [KIT:ads:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidAdsDelegate
import xxx.yyy.zzz.core.ads.CoreAds
// [KIT:ads:END]
// [KIT:pay:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidPayDelegate
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.pay.PayProductCatalog
// [KIT:pay:END]
// [KIT:push:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidPushDelegate
import xxx.yyy.zzz.core.push.CorePush
// [KIT:push:END]
// [KIT:video:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidVideoDelegate
import xxx.yyy.zzz.core.video.CoreVideo
// [KIT:video:END]
// [KIT:pag:START]
import io.github.pagkit.PagKitAndroid
import xxx.yyy.zzz.core.pag.CorePag
// [KIT:pag:END]
// [KIT_IMPORTS_END]

/**
 * Android 宿主原生 Kit 分阶段初始化参数配置。
 */
data class AndroidKitBootstrapConfig(
    val debug: Boolean = true,
    val analyticsDebug: Boolean = debug,
    val remoteConfigMinFetchIntervalSeconds: Long =
        if (debug) 0L else NativeKitBootstrap.DEFAULT_FETCH_INTERVAL_SECONDS,
    val registerFcm: Boolean? = null,
    val deferCrashReport: Boolean = false,
    val admobAppId: String = "",
    val admobBannerId: String = "",
    val admobInterstitialId: String = "",
    val admobRewardedId: String = "",
    val admobAppOpenId: String = "",
    val appsFlyerKey: String = "",
    val paySubsIds: String = "",
    val payConsumableIds: String = "",
    val payNonConsumableIds: String = "",
)

/**
 * Android 原生商业化与基础设施 Kit 引导器（NativeKitBootstrap）。
 *
 * 采用 **方案 B（双阶段分级初始化）**，与 iOS 端 `NativeKitBootstrap` 保持 1:1 对称：
 * - **Stage 1 (`initEssentialKits`)**：冷启动仅幂等装配全量 9 大 Delegate 并立即激活本地轻量必需 Kit（Log / Pag / Video / 非延迟 CrashReport）；
 * - **Stage 2 (`initDeferredKits`)**：于 Compose 首帧完成或隐私合规弹窗同意后幂等激活三方/商业化/网络敏感 Kit（Analytics / AbTesting / Mmp / Ads / Pay / Push 等）。
 */
object NativeKitBootstrap {
    const val DEFAULT_FETCH_INTERVAL_SECONDS = 3600L

    @Volatile
    private var delegatesInstalled = false

    @Volatile
    private var essentialInitialized = false

    @Volatile
    private var deferredInitialized = false

    @Volatile
    private var pendingConfig: AndroidKitBootstrapConfig? = null

    @Volatile
    private var pendingRegisterFcm: Boolean = false

    /**
     * Stage 1（冷启动阶段）：幂等安装全部平台 Delegate，并仅激活纯本地轻量与基础必需 Kit。
     */
    fun initEssentialKits(
        application: Application,
        config: AndroidKitBootstrapConfig = AndroidKitBootstrapConfig(),
    ) {
        synchronized(this) {
            val resolvedFcm = config.registerFcm ?: hasFirebaseConfig(application)
            pendingConfig = config
            pendingRegisterFcm = resolvedFcm

            if (!delegatesInstalled) {
                installDelegatesLocked(application, config)
                delegatesInstalled = true
            }

            if (!essentialInitialized) {
                initEssentialLocked(application, config.deferCrashReport)
                essentialInitialized = true
                // [KIT:log:START]
                CoreLog.i(LogTags.APP, "Android Stage 1 essential kits initialized.")
                // [KIT:log:END]
            }
        }
    }

    /**
     * Stage 2（首帧完成 / 隐私合规同意后）：幂等激活商业化、归因、远程配置与推送等三方 Kit。
     *
     * @return 若本次实际执行了激活则返回 true，尚未完成 Stage 1 或已激活过则返回 false
     */
    fun initDeferredKits(): Boolean =
        synchronized(this) {
            val config = pendingConfig
            if (deferredInitialized || !delegatesInstalled || config == null) {
                return@synchronized false
            }

            // [KIT:crashreport:START]
            if (config.deferCrashReport) {
                CoreCrashReport.doInit()
            }
            // [KIT:crashreport:END]

            // [KIT:analytics:START]
            CoreAnalytics.doInit()
            // [KIT:analytics:END]

            // [KIT:abtesting:START]
            CoreAbTesting.doInit()
            // [KIT:abtesting:END]

            // [KIT:mmp:START]
            CoreMmp.doInit(appToken = config.appsFlyerKey)
            // [KIT:mmp:END]

            // [KIT:ads:START]
            CoreAds.doInit(apiKey = config.admobAppId)
            // [KIT:ads:END]

            // [KIT:pay:START]
            CorePay.doInit(
                PayProductCatalog.fromCsv(
                    subsCsv = config.paySubsIds,
                    consumableCsv = config.payConsumableIds,
                    nonConsumableCsv = config.payNonConsumableIds,
                ),
            )
            // [KIT:pay:END]

            // [KIT:push:START]
            CorePush.doInit(registerFcm = pendingRegisterFcm)
            // [KIT:push:END]

            deferredInitialized = true
            // [KIT:log:START]
            CoreLog.i(LogTags.APP, "Android Stage 2 deferred kits initialized.")
            // [KIT:log:END]
            true
        }

    /**
     * 兼容一站式入口：依次完成 Stage 1 基础装配激活与 Stage 2 延迟 Kit 激活（常用于单测或无需延迟场景）。
     */
    fun initKits(
        application: Application,
        config: AndroidKitBootstrapConfig = AndroidKitBootstrapConfig(),
    ) {
        initEssentialKits(application, config)
        initDeferredKits()
    }

    private fun installDelegatesLocked(
        application: Application,
        config: AndroidKitBootstrapConfig,
    ) {
        // [KIT_INSTALL_START]
        // [KIT:log:START]
        CoreLog.install(AndroidLogDelegate(application))
        // [KIT:log:END]

        // [KIT:crashreport:START]
        CoreCrashReport.install(AndroidCrashReportDelegate(application, config.debug))
        // [KIT:crashreport:END]

        // [KIT:analytics:START]
        CoreAnalytics.install(AndroidAnalyticsDelegate(application, config.analyticsDebug))
        // [KIT:analytics:END]

        // [KIT:abtesting:START]
        CoreAbTesting.install(
            AndroidAbTestingDelegate(
                context = application,
                debug = config.debug,
                minFetchIntervalSeconds = config.remoteConfigMinFetchIntervalSeconds,
            ),
        )
        // [KIT:abtesting:END]

        // [KIT:mmp:START]
        CoreMmp.install(AndroidMmpDelegate(application, config.debug))
        // [KIT:mmp:END]

        // [KIT:ads:START]
        CoreAds.install(
            AndroidAdsDelegate(
                application = application,
                defaultBannerUnitId = config.admobBannerId,
                defaultInterstitialUnitId = config.admobInterstitialId,
                defaultRewardedUnitId = config.admobRewardedId,
                defaultAppOpenUnitId = config.admobAppOpenId,
                debug = config.debug,
            ),
        )
        // [KIT:ads:END]

        // [KIT:pay:START]
        CorePay.install(AndroidPayDelegate(application))
        // [KIT:pay:END]

        // [KIT:push:START]
        CorePush.install(AndroidPushDelegate(application))
        // [KIT:push:END]

        // [KIT:video:START]
        CoreVideo.install(AndroidVideoDelegate(application))
        // [KIT:video:END]
        // [KIT_INSTALL_END]
    }

    private fun initEssentialLocked(
        application: Application,
        deferCrashReport: Boolean,
    ) {
        // [KIT:log:START]
        CoreLog.doInitAllLog()
        // [KIT:log:END]

        // [KIT:crashreport:START]
        if (!deferCrashReport) {
            CoreCrashReport.doInit()
        }
        // [KIT:crashreport:END]

        // [KIT:video:START]
        CoreVideo.doInit()
        // [KIT:video:END]

        // [KIT:pag:START]
        PagKitAndroid.init(application)
        CorePag.doInit()
        // [KIT:pag:END]
    }

    /**
     * 运行时探测是否打入了 google-services 生成的 `google_app_id` 资源；
     * 由于模板默认不捆绑 google-services.json，此处无法使用静态 R.string 引用，故显式抑制 DiscouragedApi。
     */
    @SuppressLint("DiscouragedApi")
    private fun hasFirebaseConfig(application: Application): Boolean =
        application.resources.getIdentifier("google_app_id", "string", application.packageName) != 0
}
