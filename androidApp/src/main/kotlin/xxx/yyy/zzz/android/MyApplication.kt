package xxx.yyy.zzz.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import xxx.yyy.zzz.android.bridge.AndroidKitBootstrapConfig
import xxx.yyy.zzz.android.bridge.NativeKitBootstrap
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.AppEnvironment
import xxx.yyy.zzz.shared.di.AppBootstrap
import xxx.yyy.zzz.shared.performance.StartupPerformanceTracker

/**
 * Android 独立宿主 Application 入口。
 * 清晰分为：
 * 1. Stage 1：冷启动 Delegate 装配与本地轻量必需 Kit 初始化；
 * 2. Phase 2：纯 KMP Koin 容器初始化与 Stage 2 延迟 Kit 回调注册（于首帧/合规同意后自动或按需触发）。
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        StartupPerformanceTracker.markProcessStart("android")
        val isDebug = BuildConfig.DEBUG
        val isDevFlavor = BuildConfig.FLAVOR == "dev"
        val env = if (isDevFlavor) AppEnvironment.DEV else AppEnvironment.PROD

        // 1. Stage 1: 全量装配 9 大 Delegate + 激活纯本地轻量必需 Kit
        NativeKitBootstrap.initEssentialKits(
            application = this,
            config =
                AndroidKitBootstrapConfig(
                    debug = isDebug,
                    analyticsDebug = BuildConfig.ANALYTICS_DEBUG_MODE,
                    remoteConfigMinFetchIntervalSeconds = BuildConfig.REMOTE_CONFIG_MIN_FETCH_INTERVAL.toLong(),
                    admobAppId = BuildConfig.ADMOB_APP_ID,
                    admobBannerId = BuildConfig.ADMOB_BANNER_ID,
                    admobInterstitialId = BuildConfig.ADMOB_INTERSTITIAL_ID,
                    admobRewardedId = BuildConfig.ADMOB_REWARDED_ID,
                    admobAppOpenId = BuildConfig.ADMOB_APP_OPEN_ID,
                    appsFlyerKey = BuildConfig.APPSFLYER_KEY,
                    paySubsIds = BuildConfig.PAY_SUBS_IDS,
                    payConsumableIds = BuildConfig.PAY_CONSUMABLE_IDS,
                    payNonConsumableIds = BuildConfig.PAY_NON_CONSUMABLE_IDS,
                ),
        )

        // 2. 初始化已实现跨平台的纯 KMP 库 (Koin: Room / DataStore / Ktor) 并绑定 Stage 2 延迟初始化器
        val appConfig =
            AppConfig(
                env = env,
                isDebug = isDebug,
                enableHttpLog = BuildConfig.ENABLE_HTTP_LOG,
                baseUrl = BuildConfig.BASE_URL,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
            )
        AppBootstrap.doInitKoin(appConfig = appConfig) {
            androidContext(this@MyApplication)
        }
        AppBootstrap.registerDeferredKitInitializer {
            NativeKitBootstrap.initDeferredKits()
        }
        StartupPerformanceTracker.markStage1EssentialReady()
    }
}
