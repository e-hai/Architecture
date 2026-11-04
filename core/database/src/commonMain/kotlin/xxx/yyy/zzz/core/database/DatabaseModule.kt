package xxx.yyy.zzz.core.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.dsl.module

/**
 * 数据库模块 Koin 依赖注入。
 */
val coreDatabaseModule =
    module {
        single<AppDatabase> {
            val context = runCatching { get<Any>() }.getOrNull()
            getDatabaseBuilder(context)
                .setDriver(BundledSQLiteDriver())
                .build()
        }
        single<DummyDao> { get<AppDatabase>().dummyDao() }
    }
