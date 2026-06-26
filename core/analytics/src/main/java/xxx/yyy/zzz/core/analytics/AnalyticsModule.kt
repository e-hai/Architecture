package xxx.yyy.zzz.core.analytics

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreAnalyticsModule =
    module {
        single<AnalyticsHelper> {
            FirebaseAnalyticsHelper(androidContext())
        }
    }
