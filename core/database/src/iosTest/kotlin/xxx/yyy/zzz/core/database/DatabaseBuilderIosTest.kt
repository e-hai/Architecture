package xxx.yyy.zzz.core.database

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import platform.Foundation.NSFileManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * iOS 端 Room 数据库路径与持久化集成测试。
 */
class DatabaseBuilderIosTest {
    /**
     * 验证数据库存储目录位于 Library/Application Support 且已完成自动创建。
     */
    @Test
    fun databasePathUsesApplicationSupportDirectory() {
        val dirPath = resolveDatabaseDirectoryPath()
        val filePath = resolveDatabaseFilePath()

        assertTrue(dirPath.endsWith("/Library/Application Support"))
        assertFalse(dirPath.contains("/Documents"))
        assertTrue(NSFileManager.defaultManager.fileExistsAtPath(dirPath))
        assertEquals("$dirPath/app_database.db", filePath)
    }

    /**
     * 验证通过 Koin 模块组装的 iOS Room 数据库可正常写入、查询与清理。
     */
    @Test
    fun roomDatabaseReadsAndWritesInApplicationSupport() =
        runBlocking {
            val koinApp =
                koinApplication {
                    modules(coreDatabaseModule)
                }
            val database = koinApp.koin.get<AppDatabase>()
            val dao = koinApp.koin.get<DummyDao>()
            try {
                dao.clearAll()
                val sample = DummyEntity(id = "ios_test_1", title = "Title", description = "Desc")
                dao.insertDummies(listOf(sample))
                val stored = dao.getAllDummies().first()
                assertEquals(listOf(sample), stored)
                dao.clearAll()
            } finally {
                database.close()
                koinApp.close()
            }
        }
}
