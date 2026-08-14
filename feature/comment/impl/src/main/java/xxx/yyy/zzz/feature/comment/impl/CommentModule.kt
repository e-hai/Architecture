package xxx.yyy.zzz.feature.comment.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 评论模块 Koin 依赖注入声明。
 */
val commentModule =
    module {
        single<CommentRepository> { CommentRepositoryImpl() }
        viewModel { CommentViewModel(get()) }
    }
