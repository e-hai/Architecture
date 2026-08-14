package xxx.yyy.zzz.feature.profile.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 个人中心模块 Koin 依赖注入声明。
 */
val profileModule =
    module {
        single<ProfileRepository> { ProfileRepositoryImpl(get()) }
        viewModel { ProfileViewModel(get()) }
    }
