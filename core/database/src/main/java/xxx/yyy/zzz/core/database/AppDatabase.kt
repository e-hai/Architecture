package xxx.yyy.zzz.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import xxx.yyy.zzz.core.database.model.DummyEntity

/**
 * 应用数据库。
 *
 * 开发真实项目时请将 [DummyEntity] 替换为实际的 Entity 类。
 */
@Database(entities = [DummyEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
