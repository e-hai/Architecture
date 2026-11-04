package xxx.yyy.zzz.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Android 端 DataStore 路径提供与实例构建。
 */
actual fun createDataStore(context: Any?): DataStore<Preferences> {
    val appContext = (context as? Context) ?: error("Android Context must be provided to create DataStore")
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            appContext.filesDir
                .resolve("app.preferences_pb")
                .absolutePath
                .toPath()
        },
    )
}
