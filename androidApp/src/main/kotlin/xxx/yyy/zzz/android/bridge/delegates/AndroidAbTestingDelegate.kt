package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import com.kit.abtesting.AbTestingClient
import com.kit.abtesting.AbTestingConfig
import com.kit.abtesting.firebase.FirebaseAbTestingProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.abtesting.AbTestingDelegate
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.log.CoreLog

/**
 * Android A/B 测试委托实现（1:1 透传 AbTestingKit-Android 原生 SDK）。
 */
internal class AndroidAbTestingDelegate(
    private val context: Context,
    private val debug: Boolean,
) : AbTestingDelegate {
    override fun doInit() {
        runCatching {
            AbTestingClient.install(
                FirebaseAbTestingProvider(
                    context = context.applicationContext,
                    config =
                        AbTestingConfig(
                            minimumFetchIntervalInSeconds = if (debug) 0L else 3600L,
                        ),
                ),
            )
        }.onFailure {
            CoreLog.w("AbTesting", "AbTesting init skipped or failed: ${it.message}")
        }

        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                AbTestingClient.setDefaults(AbTestingKeys.defaults())
                AbTestingClient.fetchAndActivate()
            }
        }
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ) = runCatching { AbTestingClient.getBoolean(key, defaultValue) }.getOrDefault(defaultValue)

    override fun getString(
        key: String,
        defaultValue: String,
    ) = runCatching { AbTestingClient.getString(key, defaultValue) }.getOrDefault(defaultValue)

    override fun getLong(
        key: String,
        defaultValue: Long,
    ) = runCatching { AbTestingClient.getLong(key, defaultValue) }.getOrDefault(defaultValue)

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ) = runCatching { AbTestingClient.getDouble(key, defaultValue) }.getOrDefault(defaultValue)
}
