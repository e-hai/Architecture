package xxx.yyy.zzz.core.abtesting

import org.koin.dsl.module

val coreAbTestingModule =
    module {
        single<AbTestingHelper> {
            FirebaseAbTestingHelper()
        }
    }
