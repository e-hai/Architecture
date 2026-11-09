package xxx.yyy.zzz.core.ads

/**
 * 广告位触发 ID 与广告单元 ID 常量定义。
 * 包含 Google AdMob 官方测试 ID 全集 (覆盖 Android 与 iOS 双端)。
 */
object AdsKeys {
    object Trigger {
        const val HOME_BANNER = "home_banner"
        const val HOME_INTERSTITIAL = "home_interstitial"
        const val REWARD_VIDEO = "reward_video"
        const val APP_OPEN = "app_open"
    }

    /**
     * Google AdMob Android 官方公开测试 ID 全集
     */
    object AdMobAndroidTest {
        const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val BANNER = "ca-app-pub-3940256099942544/9214589741"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
        const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
        const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val MREC = "ca-app-pub-3940256099942544/2274598516"
    }

    /**
     * Google AdMob iOS 官方公开测试 ID 全集
     */
    object AdMobIosTest {
        const val APP_ID = "ca-app-pub-3940256099942544~1458002511"
        const val BANNER = "ca-app-pub-3940256099942544/2934735716"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/4411468910"
        const val REWARDED = "ca-app-pub-3940256099942544/1712485313"
        const val APP_OPEN = "ca-app-pub-3940256099942544/5575463023"
        const val NATIVE = "ca-app-pub-3940256099942544/3986624511"
    }

    /**
     * 向下兼容默认测试 ID (默认对齐 Android)
     */
    object AdMobTest {
        const val APP_ID = AdMobAndroidTest.APP_ID
        const val BANNER = AdMobAndroidTest.BANNER
        const val INTERSTITIAL = AdMobAndroidTest.INTERSTITIAL
        const val REWARDED = AdMobAndroidTest.REWARDED
        const val APP_OPEN = AdMobAndroidTest.APP_OPEN
        const val NATIVE = AdMobAndroidTest.NATIVE
        const val MREC = AdMobAndroidTest.MREC
    }
}
