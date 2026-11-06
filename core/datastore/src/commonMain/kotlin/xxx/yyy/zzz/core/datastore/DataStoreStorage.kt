package xxx.yyy.zzz.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.scope.Scope

/**
 * 跨平台 DataStore Preferences 工厂抽象（通过 Koin Scope 扩展实现平台参数安全获取）。
 */
expect fun Scope.createDataStore(): DataStore<Preferences>

/**
 * 基于 DataStore Preferences 的跨平台键值存储实现。
 */
class DataStoreStorage(
    private val dataStore: DataStore<Preferences>,
) : KeyValueStorage {
    override suspend fun putString(
        key: String,
        value: String,
    ) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun getString(key: String): String? = stringFlow(key).first()

    override fun stringFlow(key: String): Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]
        }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = booleanFlow(key, defaultValue).first()

    override fun booleanFlow(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }

    override suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
