package xxx.yyy.zzz.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.koin.core.scope.Scope
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * 解析并按需创建 iOS 应用内部持久化目录（Application Support），
 * 遵循 Apple 存储规范，避免将内部数据库文件暴露在用户的 Documents 目录中。
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun resolveDatabaseDirectoryPath(): String =
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
            "无法定位或创建 iOS Application Support 数据库目录: ${error?.localizedDescription ?: "unknown error"}"
        }
        requireNotNull(directoryUrl.path) { "iOS Application Support 数据库目录路径为空" }
    }

/**
 * 解析 iOS 端 Room 数据库文件完整路径。
 */
internal fun resolveDatabaseFilePath(fileName: String = "app_database.db"): String = "${resolveDatabaseDirectoryPath()}/$fileName"

/**
 * iOS 端数据库构造器实现。
 */
actual fun Scope.getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(
        name = resolveDatabaseFilePath(),
    )
