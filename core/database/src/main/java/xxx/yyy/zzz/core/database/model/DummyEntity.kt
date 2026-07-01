package xxx.yyy.zzz.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 占位实体，仅用于保持 Room 构建管道可工作。
 *
 * 开发真实项目时请替换为实际的 Entity 类，并相应更新 [AppDatabase] 的 entities 声明。
 */
@Entity(tableName = "dummy")
data class DummyEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String = "",
)
