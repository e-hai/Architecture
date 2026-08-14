package xxx.yyy.zzz.feature.feed.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 首页短视频模块 Koin 依赖注入声明。
 */
val feedModule =
    module {
        single<FeedRepository> { FeedRepositoryImpl(get()) }
        viewModel { FeedViewModel(get()) }
    }
