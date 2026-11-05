package xxx.yyy.zzz.shared.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.network.coreNetworkModule

/**
 * 跨平台应用依赖注入装配启动器。
 */
object AppBootstrap {
    private var isInitialized = false

    /**
     * 初始化全局 Koin 容器。
     *
     * @param appDeclaration 平台特定初始化声明（如 Android 传入 androidContext）
     */
    fun doInitKoin(appDeclaration: KoinAppDeclaration = {}) {
        if (!isInitialized) {
            isInitialized = true
            startKoin {
                appDeclaration()
                modules(
                    coreNetworkModule,
                    coreDatabaseModule,
                    coreDatastoreModule,
                )
            }
        }
    }
}
