package com.shortvideo.app.core.ads

/**
 * 广告位触发 ID（[com.kit.ads.AdsRequest.triggerId]）与广告单元 ID 常量。
 *
 * 约定：
 * - Trigger：业务语义，稳定、可读（如 `home_banner`）
 * - AdUnit：渠道后台生成的单元 ID；下列 AdMob 值为 Google 官方测试 ID，上线前必须替换
 */
object AdsKeys {
    /** 预加载 / 请求触发 ID。 */
    object Trigger {
        const val HOME_BANNER = "home_banner"
        const val HOME_INTERSTITIAL = "home_interstitial"
        const val REWARD_VIDEO = "reward_video"
        const val APP_OPEN = "app_open"
    }

    /**
     * Google AdMob 测试广告单元（Sample）。
     * @see <a href="https://developers.google.com/admob/android/test-ads">Test ads</a>
     */
    object AdMobTest {
        const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val BANNER = "ca-app-pub-3940256099942544/9214589741"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
        const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val MREC = "ca-app-pub-3940256099942544/2274598516"
    }
}
