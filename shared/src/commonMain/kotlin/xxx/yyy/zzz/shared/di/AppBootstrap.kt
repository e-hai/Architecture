package xxx.yyy.zzz.shared.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.network.coreNetworkModule

/**
 * 跨平台 KMP 核心库引导器（AppBootstrap）。
 * 专职负责已实现跨平台的纯 KMP 基础设施（Koin DI 容器：Room / DataStore / Ktor）初始化。
 */
object AppBootstrap {
    private var isInitialized = false

    /**
     * 启动纯 KMP 跨平台依赖注入容器（带防重复启动保护）。
     *
     * @param platformDeclaration 平台特定注入声明（如 Android 注入 androidContext）
     */
    fun doInitKoin(platformDeclaration: KoinAppDeclaration = {}) {
        if (!isInitialized) {
            isInitialized = true
            CoreLog.doInitAllLog()
            startKoin {
                platformDeclaration()
                modules(
                    coreNetworkModule,
                    coreDatabaseModule,
                    coreDatastoreModule,
                )
            }
            CoreLog.i(LogTags.APP, "KMP Koin DI container initialized successfully.")
        }
    }

    /**
     * 无参重载：供 iOS 等无需传入平台 Context 的宿主直接调用。
     */
    fun doInitKoin() = doInitKoin {}
}
