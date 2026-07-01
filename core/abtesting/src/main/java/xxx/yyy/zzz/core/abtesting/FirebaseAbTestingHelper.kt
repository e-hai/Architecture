package xxx.yyy.zzz.core.abtesting

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Firebase Remote Config 实现的 A/B 测试助手。
 *
 * 封装 FirebaseRemoteConfig 的具体逻辑，提供类型安全的参数获取和实验管理。
 */
class FirebaseAbTestingHelper : AbTestingHelper {

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        val config = Firebase.remoteConfig
        val configSettings: FirebaseRemoteConfigSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = DEFAULT_FETCH_INTERVAL_SECONDS
        }
        config.setConfigSettingsAsync(configSettings)
        config
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = remoteConfig.getString(key).takeIf { it.isNotEmpty() } ?: defaultValue

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean {
        return if (remoteConfig.getString(key).isEmpty()) defaultValue
        else remoteConfig.getBoolean(key)
    }

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long {
        val raw = remoteConfig.getString(key)
        return if (raw.isEmpty()) defaultValue
        else remoteConfig.getLong(key)
    }

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double {
        val raw = remoteConfig.getString(key)
        return if (raw.isEmpty()) defaultValue
        else remoteConfig.getDouble(key)
    }

    override suspend fun fetchAndActivate(): Boolean {
        return remoteConfig.fetchAndActivate().await()
    }

    override suspend fun fetch() {
        remoteConfig.fetch().await()
    }

    override suspend fun activate(): Boolean {
        return remoteConfig.activate().await()
    }

    private companion object {
        /** 默认获取间隔：生产环境 1 小时，开发环境可改为 0 以实时测试 */
        const val DEFAULT_FETCH_INTERVAL_SECONDS = 3600L
    }
}
