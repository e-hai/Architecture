package xxx.yyy.zzz.core.ads

/**
 * 跨平台广告管理委托协议。
 * 约束：仅允许在该接口的各端实现类内部调用原生 AdsKit SDK。
 */
interface AdsDelegate {
    /**
     * 初始化广告 SDK。
     *
     * @param apiKey 平台广告 API 标识 / 账户 ID
     */
    fun doInit(apiKey: String)

    fun loadAd(
        triggerId: String,
        adUnitId: String,
    )

    fun isReady(triggerId: String): Boolean

    fun showAd(triggerId: String)

    /**
     * 打开广告 SDK 原生调试 / 聚合检查面板（如 AdMob Ad Inspector / AppLovin Mediation Debugger）。
     */
    fun openDebug()
}

/**
 * 默认广告兜底实现。
 */
object DefaultAdsDelegate : AdsDelegate {
    override fun doInit(apiKey: String) = Unit

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

    override fun openDebug() {
        println("[Ads] OpenDebug (NoOp)")
    }
}

/**
 * 跨平台静态广告门面（CoreAds）。
 */
object CoreAds : AdsDelegate {
    private var delegate: AdsDelegate = DefaultAdsDelegate

    /**
     * 挂载平台特定的广告委托实现（由 Android / iOS 宿主引导器初始化时注入）。
     *
     * @param delegate 平台实现的广告委托接口
     */
    fun install(delegate: AdsDelegate) {
        this.delegate = delegate
    }

    override fun doInit(apiKey: String) = delegate.doInit(apiKey)

    override fun loadAd(
        triggerId: String,
        adUnitId: String,
    ) = delegate.loadAd(triggerId, adUnitId)

    override fun isReady(triggerId: String): Boolean = delegate.isReady(triggerId)

    override fun showAd(triggerId: String) = delegate.showAd(triggerId)

    override fun openDebug() = delegate.openDebug()
}
