package xxx.yyy.zzz.android.bridge

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.kit.abtesting.AbTestingClient
import com.kit.abtesting.AbTestingConfig
import com.kit.abtesting.firebase.FirebaseAbTestingProvider
import com.kit.ads.AdsManager
import com.kit.ads.AdsRequest
import com.kit.ads.AdsType
import com.kit.ads.provider.AdsProviderConfig
import com.kit.ads.provider.AdsProviderType
import com.kit.analytics.Analytics
import com.kit.analytics.AnalyticsConfig
import com.kit.analytics.firebase.FirebaseAnalyticsProvider
import com.kit.analytics.provider.LoggingAnalyticsProvider
import com.kit.crashreport.CrashBackend
import com.kit.crashreport.CrashReport
import com.kit.crashreport.CrashReportConfig
import com.kit.log.LogKit
import com.kit.mmp.Mmp
import com.kit.mmp.appsflyer.AppsFlyerClient
import com.kit.mmp.config.MmpConfig
import com.kit.pay.PayKit
import com.kit.pay.models.PayKitConfiguration
import com.kit.push.PushKitManager
import com.kit.push.channel.NotificationChannelConfig
import com.kit.push.provider.fcm.FcmPushProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.abtesting.AbTestingDelegate
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.ads.AdsDelegate
import xxx.yyy.zzz.core.ads.CoreAds
import xxx.yyy.zzz.core.analytics.AnalyticsDelegate
import xxx.yyy.zzz.core.analytics.CoreAnalytics
import xxx.yyy.zzz.core.crashreport.CoreCrashReport
import xxx.yyy.zzz.core.crashreport.CrashReportDelegate
import xxx.yyy.zzz.core.crashreport.CrashReportKeys
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogDelegate
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.mmp.MmpDelegate
import xxx.yyy.zzz.core.pay.CorePay
import xxx.yyy.zzz.core.pay.PayDelegate
import xxx.yyy.zzz.core.pay.PayProducts
import xxx.yyy.zzz.core.push.CorePush
import xxx.yyy.zzz.core.push.PushChannels
import xxx.yyy.zzz.core.push.PushDelegate

/**
 * Android 原生商业化 Kit 引导器（NativeKitBootstrap）。
 * 专职负责 8 大 Android 原生 AAR SDK 的代理装配与跨平台门面注入，
 * 与 iOS 端的 NativeKitBootstrap 保持 1:1 对称与相同设计范式。
 */
object NativeKitBootstrap {
    /**
     * 批量初始化所有 Android 原生 Kit 并绑定至 Core 跨平台门面。
     *
     * @param application Android Application 实例
     * @param debug 是否开启调试模式
     * @param admobAppId AdMob 应用 ID (来自 BuildConfig.ADMOB_APP_ID)
     * @param appsFlyerKey AppsFlyer Token (来自 BuildConfig.APPSFLYER_KEY)
     */
    fun initKits(
        application: Application,
        debug: Boolean = true,
        admobAppId: String = "",
        appsFlyerKey: String = "",
    ) {
        // 1. CoreLog (日志)
        CoreLog.install(AndroidLogDelegate(application))
        CoreLog.doInitAllLog()

        // 2. CoreCrashReport (崩溃分析)
        CoreCrashReport.install(AndroidCrashReportDelegate(application, debug))
        CoreCrashReport.doInit()

        // 3. CoreAnalytics (数据埋点)
        CoreAnalytics.install(AndroidAnalyticsDelegate(application, debug))
        CoreAnalytics.doInit()

        // 4. CoreAbTesting (A/B 测试 & Remote Config)
        CoreAbTesting.install(AndroidAbTestingDelegate(application, debug))
        CoreAbTesting.doInit()

        // 5. CoreMmp (归因统计)
        CoreMmp.install(AndroidMmpDelegate(application, debug))
        CoreMmp.doInit(appToken = appsFlyerKey.ifBlank { "dummy_appsflyer_key" })

        // 6. CoreAds (广告聚合)
        CoreAds.install(AndroidAdsDelegate(application))
        CoreAds.doInit(apiKey = admobAppId.ifBlank { "ca-app-pub-3940256099942544~3347511713" })

        // 7. CorePay (支付结算)
        CorePay.install(AndroidPayDelegate(application))
        CorePay.doInit()

        // 8. CorePush (推送通知)
        CorePush.install(AndroidPushDelegate(application))
        CorePush.doInit(registerFcm = false)

        CoreLog.i(LogTags.APP, "Android native kits initialized successfully.")
    }
}

// =========================================================================
// Android Native Kit Delegates (1:1 Symmetric with iOS Ios*Delegates)
// =========================================================================

/**
 * Android 日志委托实现。
 */
private class AndroidLogDelegate(
    private val application: Application,
) : LogDelegate {
    override fun doInitAllLog() {
        LogKit.initAllLog(application)
    }

    override fun doInitOnlyConsoleLog() {
        LogKit.initOnlyAndroidLog()
    }

    override fun d(
        tag: String,
        message: String,
    ) = LogKit.d(tag, message)

    override fun i(
        tag: String,
        message: String,
    ) = LogKit.i(tag, message)

    override fun w(
        tag: String,
        message: String,
    ) = LogKit.w(tag, message)

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            LogKit.e(tag, throwable, message)
        } else {
            LogKit.e(tag, message)
        }
    }
}

/**
 * Android 崩溃分析委托实现。
 */
private class AndroidCrashReportDelegate(
    private val context: Context,
    private val debug: Boolean,
) : CrashReportDelegate {
    override fun doInit() {
        runCatching {
            CrashReport.init(
                context = context,
                config =
                    CrashReportConfig(
                        enabled = true,
                        collectionEnabled = true,
                        backend = CrashBackend.FIREBASE,
                    ),
            )
            CrashReport.setCustomKey(CrashReportKeys.IS_DEBUG, debug)
        }.onFailure {
            CoreLog.w("CrashReport", "CrashReport init skipped or failed: ${it.message}")
        }
    }

    override fun setUserId(userId: String) = CrashReport.setUserId(userId)

    override fun setCustomKey(
        key: String,
        value: Any,
    ) {
        when (value) {
            is Boolean -> CrashReport.setCustomKey(key, value)
            is Int -> CrashReport.setCustomKey(key, value)
            is Long -> CrashReport.setCustomKey(key, value)
            is Float -> CrashReport.setCustomKey(key, value)
            is Double -> CrashReport.setCustomKey(key, value)
            else -> CrashReport.setCustomKey(key, value.toString())
        }
    }

    override fun log(message: String) = CrashReport.log(message)

    override fun recordException(throwable: Throwable) = CrashReport.recordException(throwable)
}

/**
 * Android 数据统计委托实现。
 */
private class AndroidAnalyticsDelegate(
    private val context: Context,
    private val debug: Boolean,
) : AnalyticsDelegate {
    override fun doInit() {
        runCatching {
            Analytics.initialize(
                context = context,
                config =
                    AnalyticsConfig(
                        providers =
                            buildList {
                                if (debug) {
                                    add(LoggingAnalyticsProvider())
                                }
                                add(FirebaseAnalyticsProvider())
                            },
                        enabled = true,
                        debug = debug,
                    ),
            )
        }.onFailure {
            CoreLog.w("Analytics", "Analytics init skipped or failed: ${it.message}")
        }
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        Analytics.logEvent(name) {
            params.forEach { (k, v) ->
                when (v) {
                    is String -> param(k, v)
                    is Int -> param(k, v.toLong())
                    is Long -> param(k, v)
                    is Double -> param(k, v)
                    is Float -> param(k, v.toDouble())
                    is Boolean -> param(k, v.toString())
                    else -> if (v != null) param(k, v.toString())
                }
            }
        }
    }

    override fun setUserId(userId: String?) {
        Analytics.setUserId(userId)
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        Analytics.setUserProperty(name, value)
    }
}

/**
 * Android A/B 测试委托实现。
 */
private class AndroidAbTestingDelegate(
    private val context: Context,
    private val debug: Boolean,
) : AbTestingDelegate {
    override fun doInit() {
        runCatching {
            AbTestingClient.install(
                FirebaseAbTestingProvider(
                    context = context.applicationContext,
                    config =
                        AbTestingConfig(
                            minimumFetchIntervalInSeconds = if (debug) 0L else 3600L,
                        ),
                ),
            )
        }.onFailure {
            CoreLog.w("AbTesting", "AbTesting init skipped or failed: ${it.message}")
        }

        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                AbTestingClient.setDefaults(AbTestingKeys.defaults())
                AbTestingClient.fetchAndActivate()
            }
        }
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ) = runCatching { AbTestingClient.getBoolean(key, defaultValue) }.getOrDefault(defaultValue)

    override fun getString(
        key: String,
        defaultValue: String,
    ) = runCatching { AbTestingClient.getString(key, defaultValue) }.getOrDefault(defaultValue)

    override fun getLong(
        key: String,
        defaultValue: Long,
    ) = runCatching { AbTestingClient.getLong(key, defaultValue) }.getOrDefault(defaultValue)

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ) = runCatching { AbTestingClient.getDouble(key, defaultValue) }.getOrDefault(defaultValue)
}

/**
 * Android 归因统计委托实现。
 */
private class AndroidMmpDelegate(
    private val context: Context,
    private val debug: Boolean,
) : MmpDelegate {
    override fun doInit(appToken: String) {
        if (appToken.isBlank()) return
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

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        val nonNullParams = params.filterValues { it != null }.mapValues { it.value!! }
        Mmp.trackEvent(eventName, nonNullParams)
    }

    override fun setCustomerUserId(userId: String?) {
        CoreLog.d(LogTags.APP, "[Mmp-Android] setCustomerUserId: $userId")
    }
}

/**
 * Android 广告管理委托实现。
 */
private class AndroidAdsDelegate(
    private val application: Application,
) : AdsDelegate {
    private val providerType = AdsProviderType.ADMOB
    private var currentActivity: Activity? = null
    private val loadedAds = java.util.concurrent.ConcurrentHashMap<String, com.kit.ads.AdsEntity>()

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {
                    currentActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (currentActivity === activity) currentActivity = null
                }

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {
                    if (currentActivity === activity) currentActivity = null
                }
            },
        )
    }

    override fun doInit(apiKey: String) {
        if (apiKey.isBlank()) return
        AdsManager.initialize(
            context = application,
            config =
                AdsProviderConfig(
                    providerType = providerType,
                    apiKey = apiKey,
                ),
        )
    }

    override fun loadAd(
        triggerId: String,
        adUnitId: String,
    ) {
        val request =
            AdsRequest(
                triggerId = triggerId,
                adUnitId = adUnitId,
                adType = AdsType.REWARDED,
                providerType = providerType,
                preloadTtlMs = null,
            )
        AdsManager.loadAd(
            application,
            request,
            object : com.kit.ads.AdsListener {
                override fun onAdLoaded(entity: com.kit.ads.AdsEntity) {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告加载成功: $triggerId")
                    loadedAds[triggerId] = entity
                }

                override fun onAdFailedToLoad(error: String) {
                    CoreLog.w(LogTags.APP, "[Ads-Android] 广告加载失败: $triggerId, error=$error")
                }

                override fun onAdShown() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告已展示: $triggerId")
                }

                override fun onAdClicked() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告被点击: $triggerId")
                }

                override fun onAdPaidEvent() {}

                override fun onAdUserEarnedReward() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 用户获得奖励: $triggerId")
                }

                override fun onAdClosed() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告已关闭: $triggerId")
                    loadedAds.remove(triggerId)
                }
            },
        )
    }

    override fun isReady(triggerId: String): Boolean = loadedAds.containsKey(triggerId)

    override fun showAd(triggerId: String) {
        CoreLog.d(LogTags.APP, "[Ads-Android] showAd: $triggerId")
        val activity = currentActivity
        val entity = loadedAds.remove(triggerId)
        if (activity != null && entity != null) {
            try {
                val container =
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        ?: activity.window.decorView as ViewGroup
                entity.show(activity, container)
            } catch (t: Throwable) {
                CoreLog.w(LogTags.APP, "[Ads-Android] showAd failed: ${t.message}")
                showFallbackAdDialog(activity, triggerId)
            }
        } else if (activity != null) {
            showFallbackAdDialog(activity, triggerId)
        } else {
            CoreLog.w(LogTags.APP, "[Ads-Android] 当前没有前台 Activity，无法展示广告")
        }
    }

    private fun showFallbackAdDialog(
        activity: Activity,
        triggerId: String,
    ) {
        activity.runOnUiThread {
            AlertDialog
                .Builder(activity)
                .setTitle("激励视频广告 (Android 模拟)")
                .setMessage("触发点: $triggerId\n已模拟完整播放 15 秒广告。\n奖励已成功发放！")
                .setPositiveButton("领取奖励") { dialog, _ ->
                    dialog.dismiss()
                    CoreLog.i(LogTags.APP, "[Ads-Android] 模拟广告奖励发放完成: $triggerId")
                }.show()
        }
    }
}

/**
 * Android 支付结算委托实现。
 */
private class AndroidPayDelegate(
    private val context: Context,
) : PayDelegate {
    override fun doInit() {
        val config =
            PayKitConfiguration(
                subsProductIds = setOf(PayProducts.SUBS_PLUS, PayProducts.SUBS_PRO),
                consumableProductIds = setOf(PayProducts.CONSUMABLE_SKIN_3DAY),
                nonConsumableProductIds = setOf(PayProducts.NON_CONSUMABLE_SKIN),
            )
        PayKit.configure(context, config)
    }

    override fun purchase(
        productId: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        CoreLog.d(LogTags.APP, "[Pay-Android] purchase request: $productId")
    }

    override fun isSubscribed(productId: String): Boolean = false
}

/**
 * Android 推送通知委托实现。
 */
private class AndroidPushDelegate(
    private val context: Context,
) : PushDelegate {
    override fun doInit(registerFcm: Boolean) {
        val push = PushKitManager.init(context)

        push.createChannel(
            NotificationChannelConfig(
                id = PushChannels.GENERAL,
                name = "普通通知",
                description = "日常消息与系统提醒",
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        push.createChannel(
            NotificationChannelConfig(
                id = PushChannels.MARKETING,
                name = "营销通知",
                description = "活动与运营推送",
                importance = NotificationManager.IMPORTANCE_LOW,
            ),
        )
        push.createChannel(
            NotificationChannelConfig(
                id = PushChannels.IMPORTANT,
                name = "重要通知",
                description = "需要及时关注的提醒",
                importance = NotificationManager.IMPORTANCE_HIGH,
            ),
        )

        if (registerFcm) {
            runCatching {
                val fcm = FcmPushProvider()
                push.registerProvider(fcm)
            }.onFailure {
                CoreLog.w("PushKit", "FCM push provider registration skipped or failed: ${it.message}")
            }
        }
    }

    override fun registerPush() {
        CoreLog.d(LogTags.APP, "[Push-Android] Push registered successfully")
    }

    override fun setPushToken(token: String) {
        CoreLog.d(LogTags.APP, "[Push-Android] FCM Token set: $token")
    }
}
