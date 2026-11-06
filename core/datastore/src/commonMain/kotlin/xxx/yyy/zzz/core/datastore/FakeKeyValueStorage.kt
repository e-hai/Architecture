package xxx.yyy.zzz.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 内存级 FakeKeyValueStorage 实现，专供单元测试与预览使用（遵循 Fake over Mock 原则）。
 */
class FakeKeyValueStorage : KeyValueStorage {
    private val data = MutableStateFlow<Map<String, Any>>(emptyMap())

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        data.value = data.value + (key to value)
    }

    override suspend fun getString(key: String): String? = data.value[key] as? String

    override fun stringFlow(key: String): Flow<String?> = data.map { it[key] as? String }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        data.value = data.value + (key to value)
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = (data.value[key] as? Boolean) ?: defaultValue

    override fun booleanFlow(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> = data.map { (it[key] as? Boolean) ?: defaultValue }

    override suspend fun remove(key: String) {
        data.value = data.value - key
    }

    override suspend fun clearAll() {
        data.value = emptyMap()
    }
}
