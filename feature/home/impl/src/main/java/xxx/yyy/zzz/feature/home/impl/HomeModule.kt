package xxx.yyy.zzz.feature.home.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureHomeModule = module {
    viewModel {
        HomeViewModel(
            getUserUseCase = get(),
            userRepository = get()
        )
    }
}
