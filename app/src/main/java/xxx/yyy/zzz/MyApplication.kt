package xxx.yyy.zzz

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import xxx.yyy.zzz.core.abtesting.coreAbTestingModule
import xxx.yyy.zzz.core.analytics.coreAnalyticsModule
import xxx.yyy.zzz.core.crashreport.coreCrashReportModule
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.network.coreNetworkModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                coreAnalyticsModule,
                coreAbTestingModule,
                coreCrashReportModule,
                coreNetworkModule,
                coreDatabaseModule,
                coreDatastoreModule,
            )
        }
    }
}
