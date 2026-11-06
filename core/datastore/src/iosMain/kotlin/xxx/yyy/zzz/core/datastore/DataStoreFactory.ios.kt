package xxx.yyy.zzz.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.scope.Scope
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS 端 DataStore 路径提供与实例构建。
 */
@OptIn(ExperimentalForeignApi::class)
actual fun Scope.createDataStore(): DataStore<Preferences> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    val filePath = requireNotNull(documentDirectory?.path) + "/app.preferences_pb"
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { filePath.toPath() },
    )
}
