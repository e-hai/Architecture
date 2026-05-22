package xxx.yyy.zzz.core.navigation

import org.koin.dsl.module

val coreNavigationModule = module {
    // Navigator must be a singleton
    single { Navigator() }
}
