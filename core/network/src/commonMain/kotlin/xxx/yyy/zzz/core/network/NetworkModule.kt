package xxx.yyy.zzz.core.network

import org.koin.dsl.module

/**
 * 网络层 Koin 依赖注入模块。
 */
val coreNetworkModule =
    module {
        single { configureHttpClient(createPlatformHttpClient()) }
        single<NetworkDataSource> { FakeNetworkDataSource() }
    }
