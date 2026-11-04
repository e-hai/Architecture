package xxx.yyy.zzz.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * 跨平台键值持久化存储门面契约。
 */
interface KeyValueStorage {
    suspend fun putString(
        key: String,
        value: String,
    )

    suspend fun getString(key: String): String?

    fun stringFlow(key: String): Flow<String?>

    suspend fun putBoolean(
        key: String,
        value: Boolean,
    )

    suspend fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean

    fun booleanFlow(
        key: String,
        defaultValue: Boolean = false,
    ): Flow<Boolean>

    suspend fun remove(key: String)

    suspend fun clearAll()
}
