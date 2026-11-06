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
        single<NetworkDataSource> { FakeNetworkDataSource() }
    }
