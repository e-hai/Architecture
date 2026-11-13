package xxx.yyy.zzz.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import xxx.yyy.zzz.android.bridge.NativeKitBootstrap
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.model.AppEnvironment
import xxx.yyy.zzz.shared.di.AppBootstrap

/**
 * Android 独立宿主 Application 入口。
 * 清晰分为「1. 原生 Kit 初始化」与「2. 跨平台 KMP 库初始化」两大标准步骤。
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val isDebug = BuildConfig.DEBUG
        val isDevFlavor = BuildConfig.FLAVOR == "dev"
        val env = if (isDevFlavor) AppEnvironment.DEV else AppEnvironment.PROD

        // 1. 初始化十大基础设施与商业化 Kit (9 大 AAR 委托 + PagKit 原生运行环境)
        NativeKitBootstrap.initKits(
            application = this,
            debug = isDebug,
            analyticsDebug = BuildConfig.ANALYTICS_DEBUG_MODE,
            remoteConfigMinFetchIntervalSeconds = BuildConfig.REMOTE_CONFIG_MIN_FETCH_INTERVAL.toLong(),
            admobAppId = BuildConfig.ADMOB_APP_ID,
            admobRewardedId = BuildConfig.ADMOB_REWARDED_ID,
            appsFlyerKey = BuildConfig.APPSFLYER_KEY,
            paySubsIds = BuildConfig.PAY_SUBS_IDS,
            payConsumableIds = BuildConfig.PAY_CONSUMABLE_IDS,
            payNonConsumableIds = BuildConfig.PAY_NON_CONSUMABLE_IDS,
        )

        // 2. 初始化已实现跨平台的纯 KMP 库 (Koin: Room / DataStore / Ktor) 并注入宿主 AppConfig
        val appConfig =
            AppConfig(
                env = env,
                isDebug = isDebug,
                baseUrl = BuildConfig.BASE_URL,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
            )
        AppBootstrap.doInitKoin(appConfig = appConfig) {
            androidContext(this@MyApplication)
        }
    }
}
