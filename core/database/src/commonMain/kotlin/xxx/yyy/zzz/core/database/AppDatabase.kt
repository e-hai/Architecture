package xxx.yyy.zzz.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.koin.core.scope.Scope

/**
 * 跨平台 Room 核心数据库。
 */
@Database(entities = [DummyEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dummyDao(): DummyDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

/**
 * 平台数据库构造器抽象（通过 Koin Scope 扩展实现平台参数安全获取）。
 */
expect fun Scope.getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
