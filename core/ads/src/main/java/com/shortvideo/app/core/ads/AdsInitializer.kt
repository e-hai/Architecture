package com.shortvideo.app.core.ads

import android.app.Application
import com.kit.ads.AdsManager
import com.kit.ads.provider.AdsProviderConfig
import com.kit.ads.provider.AdsProviderType

/**
 * AdsKit 初始化入口。
 * 默认按 AdMob 流程：`initialize` →（业务侧）`UMP.start` → `loadAd`。
 */
object AdsInitializer {
    /**
     * 初始化广告 Provider。必须在主线程调用。
     *
     * @param context Application
     * @param apiKey AdMob App ID 或 AppLovin SDK Key（与 [providerType] 对应）
     * @param providerType 默认 [AdsProviderType.ADMOB]
     * @param onResult 主线程回调，表示是否初始化成功
     */
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
        AdsManager.initialize(
            context = context,
            config =
                AdsProviderConfig(
                    providerType = providerType,
                    apiKey = apiKey,
                ),
            onResult = onResult,
        )
    }
}
