@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.network.coreNetworkModule
// [FEATURE_IMPORTS_START]
import xxx.yyy.zzz.feature.home.impl.homeModule
// [FEATURE_IMPORTS_END]

/**
 * 跨平台 KMP 核心库引导器（AppBootstrap）。
 * 专职负责已实现跨平台的纯 KMP 基础设施（Koin DI 容器：Room / DataStore / Ktor）初始化。
 */
object AppBootstrap {
    private var isInitialized = false

    /**
     * 启动纯 KMP 跨平台依赖注入容器（带防重复启动保护）。
     *
     * @param appConfig 宿主传入的应用运行时环境配置
     * @param platformDeclaration 平台特定注入声明（如 Android 注入 androidContext）
     */
    fun doInitKoin(
        appConfig: AppConfig = AppConfig(),
        platformDeclaration: KoinAppDeclaration = {},
    ) {
        if (!isInitialized) {
            isInitialized = true
            CoreLog.doInitAllLog()
            startKoin {
                platformDeclaration()
                modules(
                    module { single { appConfig } },
                    coreNetworkModule,
                    coreDatabaseModule,
                    coreDatastoreModule,
                    // [FEATURE_KOIN_MODULES_START]
                    homeModule,
                    // [FEATURE_KOIN_MODULES_END]
                )
            }
            CoreLog.i(LogTags.APP, "KMP Koin DI container initialized for env: ${appConfig.env}, debug: ${appConfig.isDebug}.")
        }
    }

    /**
     * 重载方法：使用默认配置启动。
     */
    fun doInitKoin() = doInitKoin(AppConfig()) {}

    /**
     * 供 iOS 等宿主传入配置启动。
     */
    fun doInitKoin(appConfig: AppConfig) = doInitKoin(appConfig) {}
}
