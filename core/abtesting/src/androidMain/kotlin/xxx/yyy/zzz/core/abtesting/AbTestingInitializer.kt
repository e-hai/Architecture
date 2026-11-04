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
        AndroidNativeAbTestingClient.install(
            FirebaseAbTestingProvider(
                context = context.applicationContext,
                config =
                    AbTestingConfig(
                        minimumFetchIntervalInSeconds = if (debug) 0L else 3600L,
                    ),
            ),
        )

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
                ) = AndroidNativeAbTestingClient.getBoolean(key, defaultValue)

                override fun getString(
                    key: String,
                    defaultValue: String,
                ) = AndroidNativeAbTestingClient.getString(key, defaultValue)

                override fun getLong(
                    key: String,
                    defaultValue: Long,
                ) = AndroidNativeAbTestingClient.getLong(key, defaultValue)

                override fun getDouble(
                    key: String,
                    defaultValue: Double,
                ) = AndroidNativeAbTestingClient.getDouble(key, defaultValue)
            },
        )
    }
}
