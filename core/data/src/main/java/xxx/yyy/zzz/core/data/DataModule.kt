package xxx.yyy.zzz.core.data

import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import xxx.yyy.zzz.core.domain.UserRepository

val coreDataModule = module {
    // Inject IO Dispatcher named "io"
    single(named("io")) { Dispatchers.IO }

    // Bind UserRepository interface to Repository implementation
    single {
        UserRepositoryImpl(
            userDao = get(),
            userService = get(),
            userPreferencesDataSource = get(),
            ioDispatcher = get(named("io"))
        )
    } bind UserRepository::class
}
