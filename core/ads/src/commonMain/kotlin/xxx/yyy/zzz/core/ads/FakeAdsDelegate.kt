package xxx.yyy.zzz.core.ads

/**
 * 内存可编程广告委托模拟实现（Fake over Mock），专供单元测试使用。
 */
class FakeAdsDelegate : AdsDelegate {
    var initializedApiKey: String? = null
    val loadedTriggers = mutableMapOf<String, String>()
    val loadedFormats = mutableMapOf<String, AdFormat>()
    val shownTriggers = mutableListOf<String>()
    var debugOpenedCount: Int = 0

    override fun doInit(apiKey: String) {
        initializedApiKey = apiKey
    }

    override fun loadAd(
        triggerId: String,
        adUnitId: String,
        format: AdFormat,
    ) {
        loadedTriggers[triggerId] = adUnitId
        loadedFormats[triggerId] = format
    }

    override fun isReady(triggerId: String): Boolean = loadedTriggers.containsKey(triggerId)

    override fun showAd(triggerId: String) {
        shownTriggers.add(triggerId)
        loadedTriggers.remove(triggerId)
        loadedFormats.remove(triggerId)
    }

    override fun openDebug() {
        debugOpenedCount++
    }

    fun clear() {
        initializedApiKey = null
        loadedTriggers.clear()
        loadedFormats.clear()
        shownTriggers.clear()
        debugOpenedCount = 0
    }
}
