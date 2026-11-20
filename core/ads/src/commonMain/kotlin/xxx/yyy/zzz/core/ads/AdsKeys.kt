package xxx.yyy.zzz.core.ads

/**
 * 跨平台商业化广告格式枚举（映射到底层 AdsKit 原生广告类型）。
 */
enum class AdFormat {
    REWARDED,
    INTERSTITIAL,
    APP_OPEN,
    BANNER,
}

/**
 * 广告位触发 ID 常量与默认格式映射定义。
 * 具体测试与正式 AdMob 广告单元 ID 统一外置在宿主配置中管理（Android: `config/dev.properties`；iOS: `iosApp/Config/Dev.xcconfig`）。
 */
object AdsKeys {
    object Trigger {
        const val HOME_BANNER = "home_banner"
        const val HOME_INTERSTITIAL = "home_interstitial"
        const val REWARD_VIDEO = "reward_video"
        const val APP_OPEN = "app_open"
    }

    /**
     * 根据标准触发点推断默认广告格式；未匹配的自定义触发点默认回退为 [AdFormat.REWARDED]。
     */
    fun defaultFormatFor(triggerId: String): AdFormat =
        when (triggerId) {
            Trigger.HOME_BANNER -> AdFormat.BANNER
            Trigger.HOME_INTERSTITIAL -> AdFormat.INTERSTITIAL
            Trigger.APP_OPEN -> AdFormat.APP_OPEN
            else -> AdFormat.REWARDED
        }
}
