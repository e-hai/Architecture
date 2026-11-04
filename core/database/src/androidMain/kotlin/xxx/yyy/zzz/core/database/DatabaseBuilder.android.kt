package xxx.yyy.zzz.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Android 端数据库构造器实现。
 */
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val appContext = (context as? Context) ?: error("Android Context must be provided to build AppDatabase")
    val dbFile = appContext.getDatabasePath("app_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
