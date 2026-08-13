package xxx.yyy.zzz.core.log

/**
 * 日志 Tag 常量。
 *
 * 约定：业务打印使用 `LogKit.d(LogTags.NETWORK, "...")`，禁止硬编码散落 Tag。
 */
object LogTags {
    /** Application / 进程级。 */
    const val APP = "App"

    /** 网络请求。 */
    const val NETWORK = "Network"

    /** 数据库 / DataStore。 */
    const val DATA = "Data"

    /** 导航。 */
    const val NAV = "Nav"

    /** 支付。 */
    const val PAY = "Pay"

    /** 广告。 */
    const val ADS = "Ads"

    /** 推送。 */
    const val PUSH = "Push"

    /** 归因 MMP。 */
    const val MMP = "Mmp"

    /** 统计。 */
    const val ANALYTICS = "Analytics"
}
