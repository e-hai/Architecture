package xxx.yyy.zzz.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 跨平台持久化实体类。
 *
 * @property id 唯一主键
 * @property title 标题
 * @property description 描述
 */
@Entity(tableName = "dummy_table")
data class DummyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
)

/**
 * 跨平台数据访问对象（DAO）。
 */
@Dao
interface DummyDao {
    @Query("SELECT * FROM dummy_table")
    fun getAllDummies(): Flow<List<DummyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDummies(dummies: List<DummyEntity>)

    @Query("DELETE FROM dummy_table")
    suspend fun clearAll()
}
