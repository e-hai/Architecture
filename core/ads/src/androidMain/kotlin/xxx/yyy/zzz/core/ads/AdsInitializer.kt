package xxx.yyy.zzz.core.ads

import android.app.Application
import com.kit.ads.AdsRequest
import com.kit.ads.AdsType
import com.kit.ads.provider.AdsProviderConfig
import com.kit.ads.provider.AdsProviderType
import com.kit.ads.AdsManager as AndroidNativeAdsManager

/**
 * Android 端广告初始化器。
 */
object AdsInitializer {
    fun initialize(
        context: Application,
        apiKey: String,
        providerType: AdsProviderType = AdsProviderType.ADMOB,
        onResult: ((Boolean) -> Unit)? = null,
    ) {
        if (apiKey.isBlank()) {
            onResult?.invoke(false)
            return
        }

        AndroidNativeAdsManager.initialize(
            context = context,
            config =
                AdsProviderConfig(
                    providerType = providerType,
                    apiKey = apiKey,
                ),
            onResult = onResult,
        )

        AdsManager.install(
            object : AdsDelegate {
                override fun loadAd(
                    triggerId: String,
                    adUnitId: String,
                ) {
                    AndroidNativeAdsManager.preloadAd(
                        context,
                        AdsRequest(
                            triggerId = triggerId,
                            adUnitId = adUnitId,
                            adType = AdsType.INTERSTITIAL,
                            providerType = providerType,
                            preloadTtlMs = null,
                        ),
                    )
                }

                override fun isReady(triggerId: String): Boolean = true

                override fun showAd(triggerId: String) {
                    println("[Ads-Android] showAd: $triggerId")
                }
            },
        )
    }
}
