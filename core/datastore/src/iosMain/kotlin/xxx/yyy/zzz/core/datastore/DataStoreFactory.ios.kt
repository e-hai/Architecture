package xxx.yyy.zzz.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import okio.Path.Companion.toPath
import org.koin.core.scope.Scope
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * 解析并按需创建 iOS 应用内部持久化目录（Application Support），
 * 遵循 Apple 存储规范，避免将内部偏好文件暴露在用户的 Documents 目录中。
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun resolveDataStoreDirectoryPath(): String =
    memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val directoryUrl =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSApplicationSupportDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = errorPtr.ptr,
            )
        val error = errorPtr.value
        require(directoryUrl != null && error == null) {
            "无法定位或创建 iOS Application Support 偏好目录: ${error?.localizedDescription ?: "unknown error"}"
        }
        requireNotNull(directoryUrl.path) { "iOS Application Support 偏好目录路径为空" }
    }

/**
 * 解析 iOS 端 DataStore 偏好文件完整路径。
 */
internal fun resolveDataStoreFilePath(fileName: String = "app.preferences_pb"): String = "${resolveDataStoreDirectoryPath()}/$fileName"

/**
 * iOS 端 DataStore 路径提供与实例构建。
 */
actual fun Scope.createDataStore(): DataStore<Preferences> {
    val filePath = resolveDataStoreFilePath()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { filePath.toPath() },
    )
}
