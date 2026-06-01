package xxx.yyy.zzz.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import xxx.yyy.zzz.core.database.dao.UserDao
import xxx.yyy.zzz.core.database.model.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
