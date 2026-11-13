package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import android.content.Intent
import com.kit.mmp.Mmp
import com.kit.mmp.appsflyer.AppsFlyerClient
import com.kit.mmp.attribution.AttributionData
import com.kit.mmp.config.MmpConfig
import com.kit.mmp.deeplink.DeepLinkResult
import com.kit.mmp.event.MmpRevenue
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.MmpAttribution
import xxx.yyy.zzz.core.mmp.MmpDeepLink
import xxx.yyy.zzz.core.mmp.MmpDelegate

/**
 * Android 宿主 Activity 直接深度链接（App Links / URI Scheme）转发桥接器。
 * 将原生 MmpKit Direct Deep Link API 严格封装在 delegates 目录内，由 MainActivity 在启动与热拉起时调用。
 */
internal object AndroidMmpBridge {
    fun handleDeepLink(
        context: Context,
        intent: Intent?,
    ) {
        val data = intent?.data ?: return
        CoreLog.d(LogTags.MMP, "[Mmp-Android] Forwarding direct deep link uri=$data")
        Mmp.handleDeepLink(context, intent)
    }
}

/**
 * Android 归因统计委托实现（1:1 透传 MmpKit-Android 原生 SDK）。
 */
internal class AndroidMmpDelegate(
    private val context: Context,
    private val debug: Boolean,
) : MmpDelegate {
    private val lock = Any()
    private var attributionCallback: ((MmpAttribution) -> Unit)? = null
    private var deepLinkCallback: ((MmpDeepLink) -> Unit)? = null
    private var pendingDeepLink: MmpDeepLink? = null

    override fun doInit(appToken: String) {
        if (appToken.isBlank()) {
            Mmp.setLogEnabled(debug)
            return
        }
        Mmp.init(
            context = context,
            config =
                MmpConfig(
                    appToken = appToken,
                    isDebug = debug,
                    enableLog = debug,
                ),
            client = AppsFlyerClient(),
        )
        Mmp.setAttributionListener { data ->
            val mapped = data.toCoreModel()
            CoreLog.i(
                LogTags.MMP,
                "[Mmp-Android] Attribution arrived: isOrganic=${mapped.isOrganic}, network=${mapped.network}, campaign=${mapped.campaign}",
            )
            val callback = synchronized(lock) { attributionCallback }
            callback?.invoke(mapped)
        }
        Mmp.setDeepLinkListener { result ->
            val mapped = result.toCoreModel()
            CoreLog.i(
                LogTags.MMP,
                "[Mmp-Android] DeepLink resolved: value=${mapped.deepLinkValue}, isDeferred=${mapped.isDeferred}",
            )
            val callback =
                synchronized(lock) {
                    val current = deepLinkCallback
                    if (current == null) {
                        pendingDeepLink = mapped
                    }
                    current
                }
            callback?.invoke(mapped)
        }
    }

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any?>,
    ) {
        val nonNullParams = params.filterValues { it != null }.mapValues { it.value!! }
        Mmp.trackEvent(eventName, nonNullParams)
    }

    override fun trackRevenue(
        revenue: Double,
        currency: String,
        eventName: String,
        params: Map<String, Any?>,
    ) {
        val nonNullParams = params.filterValues { it != null }.mapValues { it.value!! }
        Mmp.trackRevenue(
            MmpRevenue(
                revenue = revenue,
                currency = currency,
                eventName = eventName,
                params = nonNullParams,
            ),
        )
    }

    override fun setCustomerUserId(userId: String?) {
        val synced =
            runCatching {
                val clazz = Class.forName("com.appsflyer.AppsFlyerLib")
                val instance = clazz.getMethod("getInstance").invoke(null)
                clazz.getMethod("setCustomerUserId", String::class.java).invoke(instance, userId)
            }.onFailure {
                CoreLog.w(LogTags.MMP, "[Mmp-Android] AppsFlyerLib.setCustomerUserId unavailable: ${it.message}")
            }.isSuccess

        CoreLog.d(LogTags.MMP, "[Mmp-Android] setCustomerUserId synced=$synced, hasUserId=${!userId.isNullOrBlank()}")
    }

    override fun getAttribution(): MmpAttribution? = Mmp.attribution?.toCoreModel()

    override fun setAttributionListener(listener: ((MmpAttribution) -> Unit)?) {
        synchronized(lock) {
            attributionCallback = listener
        }
        val cached = Mmp.attribution?.toCoreModel()
        if (listener != null && cached != null) {
            listener.invoke(cached)
        }
    }

    override fun setDeepLinkListener(listener: ((MmpDeepLink) -> Unit)?) {
        val pending =
            synchronized(lock) {
                deepLinkCallback = listener
                if (listener != null) {
                    pendingDeepLink.also { pendingDeepLink = null }
                } else {
                    null
                }
            }
        if (listener != null && pending != null) {
            listener.invoke(pending)
        }
    }

    private fun AttributionData.toCoreModel(): MmpAttribution =
        MmpAttribution(
            network = network,
            campaign = campaign,
            adGroup = adGroup,
            creative = creative,
            clickLabel = clickLabel,
            isOrganic = isOrganic,
        )

    private fun DeepLinkResult.toCoreModel(): MmpDeepLink =
        MmpDeepLink(
            deepLinkValue = deepLinkValue,
            params = clickEvent.filterValues { it != null }.mapValues { it.value.toString() },
            isDeferred = isDeferred,
        )
}
