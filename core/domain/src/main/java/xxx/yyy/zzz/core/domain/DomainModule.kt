package xxx.yyy.zzz.core.domain

import org.koin.dsl.module

val coreDomainModule = module {
    // UseCases are stateless — use factory for a fresh instance per injection
    factory { GetUserUseCase(userRepository = get()) }
}
