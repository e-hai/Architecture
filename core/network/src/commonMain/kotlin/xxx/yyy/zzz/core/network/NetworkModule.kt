package xxx.yyy.zzz.core.network

import org.koin.dsl.module
import xxx.yyy.zzz.core.model.AppConfig

/**
 * 网络层 Koin 依赖注入模块。
 */
val coreNetworkModule =
    module {
        single {
            val appConfig = getOrNull<AppConfig>()
            configureHttpClient(createPlatformHttpClient(), appConfig)
        }
        // 提供真实 Ktor 实现供业务按需切换；模板默认绑定 FakeNetworkDataSource 以免占位域名阻塞开箱运行
        single { KtorNetworkDataSource(get()) }
        single<NetworkDataSource> { FakeNetworkDataSource() }
    }
