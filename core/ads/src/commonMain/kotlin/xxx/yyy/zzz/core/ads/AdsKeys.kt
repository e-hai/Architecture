package xxx.yyy.zzz.core.ads

/**
 * 广告位触发 ID 常量定义。
 * 具体测试与正式 AdMob 广告单元 ID 统一外置在宿主配置中管理（Android: `config/dev.properties`；iOS: `iosApp/Config/Dev.xcconfig`）。
 */
object AdsKeys {
    object Trigger {
        const val HOME_BANNER = "home_banner"
        const val HOME_INTERSTITIAL = "home_interstitial"
        const val REWARD_VIDEO = "reward_video"
        const val APP_OPEN = "app_open"
    }
}
