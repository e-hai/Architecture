package xxx.yyy.zzz.shared.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.log.LogKit
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.network.coreNetworkModule

/**
 * 跨平台应用统一启动器（AppBootstrap）。
 * 统一调度双端标准三阶段生命周期：
 * 1. 平台原生 Kit / Bridge 挂载 (Platform Kits / Bridges)
 * 2. Koin 依赖注入容器启动 (Dependency Injection)
 * 3. 业务就绪与冷启动确认 (App Ready)
 */
object AppBootstrap {
    private var isInitialized = false

    /**
     * 基础跨平台依赖注入容器启动（带防重复启动保护）。
     *
     * @param platformDeclaration 平台特定注入声明（如 Android 注入 androidContext）
     */
    fun doInitKoin(platformDeclaration: KoinAppDeclaration = {}) {
        if (!isInitialized) {
            isInitialized = true
            startKoin {
                platformDeclaration()
                modules(
                    coreNetworkModule,
                    coreDatabaseModule,
                    coreDatastoreModule,
                )
            }
            LogKit.i(LogTags.APP, "Koin DI container initialized successfully.")
        }
    }

    /**
     * 通用跨平台初始化（供 iOS / 桌面端 / 单元测试等无 Android Context 的环境统一调用）。
     */
    fun initCommon() {
        doInitKoin()
        LogKit.i(LogTags.APP, "Application bootstrap completed on Common/iOS.")
    }
}
