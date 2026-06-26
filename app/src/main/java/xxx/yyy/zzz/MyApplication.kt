package xxx.yyy.zzz

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import xxx.yyy.zzz.core.analytics.coreAnalyticsModule
import xxx.yyy.zzz.core.data.coreDataModule
import xxx.yyy.zzz.core.database.coreDatabaseModule
import xxx.yyy.zzz.core.datastore.coreDatastoreModule
import xxx.yyy.zzz.core.network.coreNetworkModule
import xxx.yyy.zzz.feature.home.impl.featureHomeModule
import xxx.yyy.zzz.feature.settings.impl.featureSettingsModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                // Core Infrastructure
                coreAnalyticsModule,
                coreNetworkModule,
                coreDatabaseModule,
                coreDatastoreModule,
                coreDataModule,
                // Feature Modules
                featureHomeModule,
                featureSettingsModule,
            )
        }
    }
}
