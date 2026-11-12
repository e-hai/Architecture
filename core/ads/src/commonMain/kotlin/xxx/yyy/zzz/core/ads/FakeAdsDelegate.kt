package xxx.yyy.zzz.core.ads

/**
 * 内存可编程广告委托模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeAdsDelegate : AdsDelegate {
    var initializedApiKey: String? = null
    val loadedTriggers = mutableMapOf<String, String>()
    val shownTriggers = mutableListOf<String>()
    var debugOpenedCount: Int = 0

    override fun doInit(apiKey: String) {
        initializedApiKey = apiKey
    }

    override fun loadAd(
        triggerId: String,
        adUnitId: String,
    ) {
        loadedTriggers[triggerId] = adUnitId
    }

    override fun isReady(triggerId: String): Boolean = loadedTriggers.containsKey(triggerId)

    override fun showAd(triggerId: String) {
        shownTriggers.add(triggerId)
        loadedTriggers.remove(triggerId)
    }

    override fun openDebug() {
        debugOpenedCount++
    }

    fun clear() {
        initializedApiKey = null
        loadedTriggers.clear()
        shownTriggers.clear()
        debugOpenedCount = 0
    }
}
