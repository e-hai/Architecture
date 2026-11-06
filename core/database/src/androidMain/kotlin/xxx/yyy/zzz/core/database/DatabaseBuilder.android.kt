package xxx.yyy.zzz.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.scope.Scope

/**
 * Android 端数据库构造器实现（在 Koin Scope 内安全获取 Context）。
 */
actual fun Scope.getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = get<Context>()
    val dbFile = appContext.getDatabasePath("app_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
