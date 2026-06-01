package xxx.yyy.zzz.core.data

import kotlinx.coroutines.Dispatchers
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    // Bind UserRepository interface to Repository implementation
    single {
        UserRepositoryImpl(
            userDao = get(),
            userService = get(),
            userPreferencesDataSource = get(),
            ioDispatcher = Dispatchers.IO
        )
    } bind UserRepository::class
}
