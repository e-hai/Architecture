package xxx.yyy.zzz.core.ads

/**
 * 跨平台广告管理委托协议。
 */
interface AdsDelegate {
    fun loadAd(
        triggerId: String,
        adUnitId: String,
    )

    fun isReady(triggerId: String): Boolean

    fun showAd(triggerId: String)
}

/**
 * 默认广告兜底实现。
 */
object DefaultAdsDelegate : AdsDelegate {
    override fun loadAd(
        triggerId: String,
        adUnitId: String,
    ) {
        println("[Ads] LoadAd: triggerId=$triggerId, unitId=$adUnitId")
    }

    override fun isReady(triggerId: String): Boolean = false

    override fun showAd(triggerId: String) {
        println("[Ads] ShowAd: triggerId=$triggerId (NoOp)")
    }
}

/**
 * 跨平台静态广告门面。
 */
object AdsManager : AdsDelegate {
    private var delegate: AdsDelegate = DefaultAdsDelegate

    fun install(platformDelegate: AdsDelegate) {
        delegate = platformDelegate
    }

    override fun loadAd(
        triggerId: String,
        adUnitId: String,
    ) = delegate.loadAd(triggerId, adUnitId)

    override fun isReady(triggerId: String): Boolean = delegate.isReady(triggerId)

    override fun showAd(triggerId: String) = delegate.showAd(triggerId)
}
