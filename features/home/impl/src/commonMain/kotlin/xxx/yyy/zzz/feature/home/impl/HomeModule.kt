package xxx.yyy.zzz.feature.home.impl

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 首页依赖注入模块。
 */
val homeModule =
    module {
        viewModelOf(::HomeViewModel)
    }
