package xxx.yyy.zzz.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS 端数据库构造器实现。
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    val dbFilePath = requireNotNull(documentDirectory?.path) + "/app_database.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}
