package com.shortvideo.app.feature.discover.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 发现探索模块 Koin 依赖注入声明。
 */
val discoverModule =
    module {
        single<DiscoverRepository> { DiscoverRepositoryImpl(get()) }
        viewModel { DiscoverViewModel(get()) }
    }
