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
import xxx.yyy.zzz.shared.debug.DebugPanelBridge
// [FEATURE_IMPORTS_START]
// [FEATURE:home:START]
import xxx.yyy.zzz.feature.home.impl.homeModule
// [FEATURE:home:END]
// [FEATURE_IMPORTS_END]

/**
 * 跨平台 KMP 核心库引导器（AppBootstrap）。
 * 专职负责已实现跨平台的纯 KMP 基础设施（Koin DI 容器：Room / DataStore / Ktor）与各业务 Feature 模块初始化。
 */
object AppBootstrap {
    private var isInitialized = false
    private var isDeferredInitialized = false
    private var deferredKitInitializer: (() -> Unit)? = null

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
                    // [FEATURE:home:START]
                    homeModule,
                    // [FEATURE:home:END]
                    // [FEATURE_KOIN_MODULES_END]
                    *DebugPanelBridge.koinModules.toTypedArray(),
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

    /**
     * 注册宿主 Stage 2 延迟初始化回调（商业化、统计、推送与归因等三方 Kit），
     * 供 Compose 首帧完成或隐私合规弹窗同意后跨平台统一触发。
     */
    fun registerDeferredKitInitializer(initializer: () -> Unit) {
        deferredKitInitializer = initializer
        if (isDeferredInitialized) {
            initializer()
        }
    }

    /**
     * 触发 Stage 2 延迟 Kit 初始化（幂等保护，最多执行一次；显式以 `doInit*` 命名规避 K/N Obj-C 构造器改名）。
     *
     * @return 若本次实际执行了初始化回调则返回 true，已初始化或尚未注册回调则返回 false
     */
    fun doInitDeferredKits(): Boolean {
        val initializer = deferredKitInitializer
        return if (!isDeferredInitialized && initializer != null) {
            isDeferredInitialized = true
            initializer()
            CoreLog.i(LogTags.APP, "Stage 2 deferred kits triggered from KMP AppBootstrap.")
            true
        } else {
            false
        }
    }

    /**
     * 查询 Stage 2 延迟 Kit 是否已触发激活。
     */
    fun isDeferredKitsInitialized(): Boolean = isDeferredInitialized

    internal fun resetForTesting() {
        isInitialized = false
        isDeferredInitialized = false
        deferredKitInitializer = null
    }
}
