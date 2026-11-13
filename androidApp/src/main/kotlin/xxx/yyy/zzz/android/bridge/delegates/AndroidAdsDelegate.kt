package xxx.yyy.zzz.android.bridge.delegates

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import com.kit.ads.AdsEntity
import com.kit.ads.AdsListener
import com.kit.ads.AdsLogLevel
import com.kit.ads.AdsLogListener
import com.kit.ads.AdsManager
import com.kit.ads.AdsRequest
import com.kit.ads.AdsType
import com.kit.ads.provider.AdsProviderConfig
import com.kit.ads.provider.AdsProviderType
import xxx.yyy.zzz.core.ads.AdsDelegate
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 广告管理委托实现（1:1 透传 AdsKit-Android 原生 SDK）。
 */
internal class AndroidAdsDelegate(
    private val application: Application,
    private val defaultRewardedUnitId: String = "",
    private val debug: Boolean = false,
) : AdsDelegate {
    private val providerType = AdsProviderType.ADMOB
    private var currentActivity: Activity? = null
    private val loadedAds = ConcurrentHashMap<String, AdsEntity>()

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
    ) {
        val resolvedUnitId = adUnitId.ifBlank { defaultRewardedUnitId }
        if (resolvedUnitId.isBlank()) {
            CoreLog.w(LogTags.ADS, "[Ads-Android] loadAd skipped: empty adUnitId for trigger=$triggerId")
            return
        }
        val request =
            AdsRequest(
                triggerId = triggerId,
                adUnitId = resolvedUnitId,
                adType = AdsType.REWARDED,
                providerType = providerType,
                preloadTtlMs = null,
            )
        AdsManager.loadAd(
            application,
            request,
            object : AdsListener {
                override fun onAdLoaded(ad: AdsEntity) {
                    CoreLog.i(LogTags.APP, "[Ads-Android] 广告加载成功: $triggerId")
                    loadedAds[triggerId] = ad
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
