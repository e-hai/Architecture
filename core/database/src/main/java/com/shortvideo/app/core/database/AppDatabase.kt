package com.shortvideo.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shortvideo.app.core.database.dao.VideoDao
import com.shortvideo.app.core.database.model.DummyEntity
import com.shortvideo.app.core.database.model.VideoEntity

/**
 * 应用数据库。
 * 作为离线优先架构的本地持久化中心与单一可信源。
 */
@Database(
    entities = [
        DummyEntity::class,
        VideoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 获取短视频 DAO 实例。
     */
    abstract fun videoDao(): VideoDao
}
