package xxx.yyy.zzz.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.scope.Scope

/**
 * Android 端 DataStore 路径提供与实例构建（在 Koin Scope 内安全获取 Context）。
 */
actual fun Scope.createDataStore(): DataStore<Preferences> {
    val appContext = get<Context>()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            appContext.filesDir
                .resolve("app.preferences_pb")
                .absolutePath
                .toPath()
        },
    )
}
