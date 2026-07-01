package xxx.yyy.zzz.core.crashreport

import org.koin.dsl.module

val coreCrashReportModule =
    module {
        single<CrashReportHelper> {
            FirebaseCrashReportHelper()
        }
    }
