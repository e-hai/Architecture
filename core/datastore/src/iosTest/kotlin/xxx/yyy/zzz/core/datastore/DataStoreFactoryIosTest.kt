package xxx.yyy.zzz.core.datastore

import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import platform.Foundation.NSFileManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * iOS 端 DataStore 路径与键值持久化集成测试。
 */
class DataStoreFactoryIosTest {
    /**
     * 验证 DataStore 存储目录位于 Library/Application Support 且已完成自动创建。
     */
    @Test
    fun dataStorePathUsesApplicationSupportDirectory() {
        val dirPath = resolveDataStoreDirectoryPath()
        val filePath = resolveDataStoreFilePath()

        assertTrue(dirPath.endsWith("/Library/Application Support"))
        assertFalse(dirPath.contains("/Documents"))
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(dirPath))
        assertEquals("$dirPath/app.preferences_pb", filePath)
    }

    /**
     * 验证通过 Koin 模块组装的 iOS DataStore 可在 Application Support 正常读写与清理。
     */
    @Test
    fun dataStoreReadsAndWritesInApplicationSupport() =
        runBlocking {
            val koinApp =
                koinApplication {
                    modules(coreDatastoreModule)
                }
            val storage = koinApp.koin.get<KeyValueStorage>()
            try {
                val key = "ios_datastore_smoke_key"
                storage.putString(key, "ok")
                assertEquals("ok", storage.getString(key))
                storage.remove(key)
                assertNull(storage.getString(key))
            } finally {
                koinApp.close()
            }
        }
}
