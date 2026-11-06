package xxx.yyy.zzz.core.datastore

import org.koin.dsl.module

/**
 * 键值存储模块 Koin 依赖注入。
 */
val coreDatastoreModule =
    module {
        single { createDataStore() }
        single<KeyValueStorage> { DataStoreStorage(get()) }
    }
