package xxx.yyy.zzz.core.abtesting

import android.content.Context
import com.kit.abtesting.AbTestingConfig
import com.kit.abtesting.firebase.FirebaseAbTestingProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.kit.abtesting.AbTestingClient as AndroidNativeAbTestingClient

/**
 * Android 端 A/B 测试初始化器。
 */
object AbTestingInitializer {
    fun initialize(
        context: Context,
        debug: Boolean,
        defaults: Map<String, Any> = AbTestingKeys.defaults(),
    ) {
        runCatching {
            AndroidNativeAbTestingClient.install(
                FirebaseAbTestingProvider(
                    context = context.applicationContext,
                    config =
                        AbTestingConfig(
                            minimumFetchIntervalInSeconds = if (debug) 0L else 3600L,
                        ),
                ),
            )
        }.onFailure {
            android.util.Log.w("AbTesting", "AbTesting init skipped or failed: ${it.message}")
        }

        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                AndroidNativeAbTestingClient.setDefaults(defaults)
                AndroidNativeAbTestingClient.fetchAndActivate()
            }
        }

        AbTestingClient.install(
            object : AbTestingDelegate {
                override fun getBoolean(
                    key: String,
                    defaultValue: Boolean,
                ) = runCatching { AndroidNativeAbTestingClient.getBoolean(key, defaultValue) }.getOrDefault(defaultValue)

                override fun getString(
                    key: String,
                    defaultValue: String,
                ) = runCatching { AndroidNativeAbTestingClient.getString(key, defaultValue) }.getOrDefault(defaultValue)

                override fun getLong(
                    key: String,
                    defaultValue: Long,
                ) = runCatching { AndroidNativeAbTestingClient.getLong(key, defaultValue) }.getOrDefault(defaultValue)

                override fun getDouble(
                    key: String,
                    defaultValue: Double,
                ) = runCatching { AndroidNativeAbTestingClient.getDouble(key, defaultValue) }.getOrDefault(defaultValue)
            },
        )
    }
}
