package com.shortvideo.app.feature.creator.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 创作模块 Koin 依赖注入声明。
 */
val creatorModule =
    module {
        single<CreatorRepository> { CreatorRepositoryImpl(get()) }
        viewModel { CreatorViewModel(get()) }
    }
