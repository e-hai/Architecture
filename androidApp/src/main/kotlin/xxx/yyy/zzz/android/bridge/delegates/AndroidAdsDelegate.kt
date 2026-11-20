package xxx.yyy.zzz.android.bridge.delegates

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.kit.ads.AdsEntity
import com.kit.ads.AdsListener
import com.kit.ads.AdsLogLevel
import com.kit.ads.AdsLogListener
import com.kit.ads.AdsManager
import com.kit.ads.AdsRequest
import com.kit.ads.AdsType
import com.kit.ads.provider.AdsProviderConfig
import com.kit.ads.provider.AdsProviderType
import xxx.yyy.zzz.core.ads.AdFormat
import xxx.yyy.zzz.core.ads.AdsDelegate
import xxx.yyy.zzz.core.ads.AdsKeys
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 广告管理委托实现（1:1 透传 AdsKit-Android 原生 SDK，支持多广告格式映射与独立 Banner 容器）。
 */
internal class AndroidAdsDelegate(
    private val application: Application,
    defaultBannerUnitId: String = "",
    defaultInterstitialUnitId: String = "",
    defaultRewardedUnitId: String = "",
    defaultAppOpenUnitId: String = "",
    private val debug: Boolean = false,
) : AdsDelegate {
    private data class LoadedAdEntry(
        val entity: AdsEntity,
        val format: AdFormat,
    )

    private val defaultUnitIds: Map<AdFormat, String> =
        mapOf(
            AdFormat.BANNER to defaultBannerUnitId,
            AdFormat.INTERSTITIAL to defaultInterstitialUnitId,
            AdFormat.REWARDED to defaultRewardedUnitId,
            AdFormat.APP_OPEN to defaultAppOpenUnitId,
        )
    private val providerType = AdsProviderType.ADMOB
    private var currentActivity: Activity? = null
    private val loadedAds = ConcurrentHashMap<String, LoadedAdEntry>()
    private var activeBannerEntity: AdsEntity? = null

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
            logEnabled = debug,
            debugEnabled = debug,
            logListener =
                AdsLogListener { level, tag, message, throwable ->
                    val formatted = "[$tag] $message"
                    when (level) {
                        AdsLogLevel.DEBUG -> CoreLog.d(LogTags.APP, formatted)
                        AdsLogLevel.WARN -> CoreLog.w(LogTags.APP, formatted)
                        AdsLogLevel.ERROR -> CoreLog.e(LogTags.APP, formatted, throwable)
                    }
                },
        )
    }

    override fun loadAd(
        triggerId: String,
        adUnitId: String,
        format: AdFormat,
    ) {
        val resolvedUnitId = adUnitId.ifBlank { defaultUnitIds[format].orEmpty() }.trim()
        if (resolvedUnitId.isBlank()) {
            CoreLog.w(LogTags.ADS, "[Ads-Android] loadAd skipped: empty adUnitId for trigger=$triggerId, format=$format")
            return
        }
        val request =
            AdsRequest(
                triggerId = triggerId,
                adUnitId = resolvedUnitId,
                adType = format.toSdkAdsType(),
                providerType = providerType,
                preloadTtlMs = null,
            )
        AdsManager.loadAd(
            application,
            request,
            object : AdsListener {
                override fun onAdLoaded(ad: AdsEntity) {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告加载成功 ($format): $triggerId")
                    loadedAds[triggerId] = LoadedAdEntry(entity = ad, format = format)
                }

                override fun onAdFailedToLoad(error: String) {
                    CoreLog.w(LogTags.APP, "[Ads-Android] 广告加载失败 ($format): $triggerId, error=$error")
                }

                override fun onAdShown() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告已展示 ($format): $triggerId")
                }

                override fun onAdClicked() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告被点击 ($format): $triggerId")
                }

                override fun onAdPaidEvent() {}

                override fun onAdUserEarnedReward() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 用户获得奖励 ($format): $triggerId")
                }

                override fun onAdClosed() {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告已关闭 ($format): $triggerId")
                    loadedAds.remove(triggerId)
                }
            },
        )
    }

    override fun isReady(triggerId: String): Boolean = loadedAds.containsKey(triggerId)

    override fun showAd(triggerId: String) {
        CoreLog.d(LogTags.APP, "[Ads-Android] showAd: $triggerId")
        val activity = currentActivity
        val entry = loadedAds.remove(triggerId)
        val format = entry?.format ?: AdsKeys.defaultFormatFor(triggerId)
        if (activity != null && entry != null) {
            try {
                if (format == AdFormat.BANNER) {
                    activity.runOnUiThread {
                        activeBannerEntity?.destroy()
                        activeBannerEntity = entry.entity
                        val container = getOrCreateBannerContainer(activity)
                        container.removeAllViews()
                        entry.entity.show(activity, container)
                    }
                } else {
                    val container =
                        activity.findViewById<ViewGroup>(android.R.id.content)
                            ?: activity.window.decorView as ViewGroup
                    entry.entity.show(activity, container)
                }
            } catch (t: Throwable) {
                CoreLog.w(LogTags.APP, "[Ads-Android] showAd failed ($format): ${t.message}")
                showFallbackAdDialog(activity, triggerId, format)
            }
        } else if (activity != null) {
            showFallbackAdDialog(activity, triggerId, format)
        } else {
            CoreLog.w(LogTags.APP, "[Ads-Android] 当前没有前台 Activity，无法展示广告")
        }
    }

    override fun openDebug() {
        val activity = currentActivity
        if (activity != null) {
            activity.runOnUiThread {
                AdsManager.openDebug(activity)
            }
        } else {
            CoreLog.w(LogTags.APP, "[Ads-Android] 当前没有前台 Activity，无法打开广告调试面板")
        }
    }

    private fun getOrCreateBannerContainer(activity: Activity): FrameLayout {
        val contentRoot =
            activity.findViewById<ViewGroup>(android.R.id.content)
                ?: activity.window.decorView as ViewGroup
        contentRoot.findViewWithTag<FrameLayout>(BANNER_CONTAINER_TAG)?.let { return it }
        return FrameLayout(activity).apply {
            tag = BANNER_CONTAINER_TAG
            val lp =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                )
            contentRoot.addView(this, lp)
        }
    }

    private fun AdFormat.toSdkAdsType(): AdsType =
        when (this) {
            AdFormat.REWARDED -> AdsType.REWARDED
            AdFormat.INTERSTITIAL -> AdsType.INTERSTITIAL
            AdFormat.APP_OPEN -> AdsType.SPLASH
            AdFormat.BANNER -> AdsType.BANNER
        }

    private fun AdFormat.displayTitle(): String =
        when (this) {
            AdFormat.REWARDED -> "激励视频广告 (Android 模拟)"
            AdFormat.INTERSTITIAL -> "插屏广告 (Android 模拟)"
            AdFormat.APP_OPEN -> "开屏广告 (Android 模拟)"
            AdFormat.BANNER -> "横幅广告 (Android 模拟)"
        }

    private fun showFallbackAdDialog(
        activity: Activity,
        triggerId: String,
        format: AdFormat,
    ) {
        activity.runOnUiThread {
            val actionText = if (format == AdFormat.REWARDED) "领取奖励" else "关闭"
            AlertDialog
                .Builder(activity)
                .setTitle(format.displayTitle())
                .setMessage("触发点: $triggerId\n广告格式: $format\n当前暂未缓存到真实广告，已触发调试模拟展示。")
                .setPositiveButton(actionText) { dialog, _ ->
                    dialog.dismiss()
                    CoreLog.i(LogTags.APP, "[Ads-Android] 模拟广告展示完成 ($format): $triggerId")
                }.show()
        }
    }

    private companion object {
        const val BANNER_CONTAINER_TAG = "core_ads_banner_container"
    }
}
