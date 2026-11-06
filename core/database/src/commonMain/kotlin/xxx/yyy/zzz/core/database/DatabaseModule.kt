package xxx.yyy.zzz.core.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.dsl.module

/**
 * 数据库模块 Koin 依赖注入。
 */
val coreDatabaseModule =
    module {
        single<AppDatabase> {
            getDatabaseBuilder()
                .setDriver(BundledSQLiteDriver())
                .build()
        }
        single<DummyDao> { get<AppDatabase>().dummyDao() }
    }
