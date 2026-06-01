# Core Database 模块

Room 数据库模块，提供本地数据持久化能力。

---

## 📦 模块结构

```
core/database/
├── src/main/java/xxx/yyy/zzz/core/database/
│   ├── model/                    # Entity 实体类
│   │   └── UserEntity.kt
│   ├── dao/                      # DAO 接口
│   │   └── UserDao.kt
│   ├── AppDatabase.kt            # Room 数据库定义
│   └── DatabaseModule.kt         # Koin 依赖注入模块
├── schemas/                      # Room schema 导出目录
│   └── xxx.yyy.zzz.core.database.AppDatabase/
│       └── 1.json
├── build.gradle.kts
└── README.md                     # 本文件
```

---

## 🏗️ 目录结构规范

### `model/` 文件夹
存放所有 Room Entity 实体类。

**命名规范：**
- **必须以 `Entity` 作为类名后缀**（例如：`UserEntity`、`ArticleEntity`）
- 文件名与类名保持一致（例如：`UserEntity.kt`）

**示例：**
```kotlin
package xxx.yyy.zzz.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import xxx.yyy.zzz.core.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String
) {
    fun toDomainModel(): User = User(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl
    )
}

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl
)
```

**规范：**
- 每个 Entity 对应一个数据表
- 提供与领域模型（Domain Model）的转换函数
- 使用 `@Entity` 注解并指定表名

### `dao/` 文件夹
存放所有 Data Access Object (DAO) 接口。

**示例：**
```kotlin
package xxx.yyy.zzz.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xxx.yyy.zzz.core.database.model.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserStream(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}
```

**规范：**
- 每个 Entity 对应一个 DAO 接口
- 查询方法返回 `Flow` 以支持响应式数据流
- 写入方法使用 `suspend` 修饰符
- 使用 `OnConflictStrategy.REPLACE` 处理冲突

### 根目录文件

#### `AppDatabase.kt`
Room 数据库主类，声明所有 Entity 和 DAO。

```kotlin
package xxx.yyy.zzz.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import xxx.yyy.zzz.core.database.dao.UserDao
import xxx.yyy.zzz.core.database.model.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

#### `DatabaseModule.kt`
Koin 依赖注入模块，提供数据库和 DAO 实例。

```kotlin
package xxx.yyy.zzz.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    single {
        get<AppDatabase>().userDao()
    }
}
```

---

## 💡 使用示例

### 在 Repository 中使用

```kotlin
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userService: UserService,
    private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    
    override fun getUserStream(userId: String): Flow<User?> = flow {
        // 先从数据库获取缓存数据
        userDao.getUserStream(userId)
            .map { it?.toDomainModel() }
            .collect { emit(it) }
    }.flowOn(ioDispatcher)
    
    override suspend fun refreshUser(userId: String) {
        withContext(ioDispatcher) {
            try {
                val userResponse = userService.getUser(userId)
                userDao.insertUser(userResponse.toEntity())
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}
```

### 添加新的 Entity 和 DAO

1. **在 `model/` 中创建 Entity：**
```kotlin
// model/ArticleEntity.kt
package xxx.yyy.zzz.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdAt: Long
)
```

2. **在 `dao/` 中创建 DAO：**
```kotlin
// dao/ArticleDao.kt
package xxx.yyy.zzz.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>
    
    @Insert
    suspend fun insertArticle(article: ArticleEntity)
}
```

3. **在 `AppDatabase` 中注册：**
```kotlin
@Database(
    entities = [UserEntity::class, ArticleEntity::class],
    version = 2,  // 记得增加版本号
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun articleDao(): ArticleDao  // 添加新的 DAO
}
```

4. **在 `DatabaseModule` 中提供 DAO：**
```kotlin
val coreDatabaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().articleDao() }  // 添加新的 DAO
}
```

---

## ⚠️ 注意事项

### 1. Schema 导出
- `exportSchema = true` 确保 Room 生成 schema 文件
- Schema 文件位于 `schemas/` 目录
- 用于数据库迁移测试和版本管理

### 2. 数据库迁移

#### 何时需要迁移？

以下情况**必须**进行数据库迁移：

1. **添加新表**
   ```kotlin
   // 新增 ArticleEntity
   @Database(entities = [UserEntity::class, ArticleEntity::class], version = 2)
   ```

2. **删除表**
   ```kotlin
   // 移除 UserEntity
   @Database(entities = [ArticleEntity::class], version = 2)
   ```

3. **修改表的列**
   ```kotlin
   // UserEntity 添加新字段
   data class UserEntity(
       @PrimaryKey val id: String,
       val name: String,
       val age: Int? = null  // 新增可选字段
   )
   ```

4. **修改列的类型或约束**
   ```kotlin
   // 修改主键类型
   @PrimaryKey val id: Long  // 原来是 String
   ```

5. **重命名表或列**
   ```kotlin
   @Entity(tableName = "users_new")  // 原来是 "users"
   ```

**不需要迁移的情况：**
- 仅添加新的 DAO 方法
- 修改查询语句（SQL）
- 添加索引（使用 `@Index` 但需要增加版本号）

---

#### Migration vs AutoMigration

Room 提供两种迁移方式，各有适用场景：

##### **手动 Migration（推荐用于复杂变更）**

**优点：**
- ✅ 完全控制迁移逻辑
- ✅ 可以处理数据转换和清理
- ✅ 可以保留重要数据
- ✅ 适合复杂的 schema 变更

**缺点：**
- ❌ 需要编写 SQL 代码
- ❌ 容易出错，需要充分测试
- ❌ 维护成本较高

**使用示例：**
```kotlin
// 创建 Migration 对象
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新表
        database.execSQL("""
            CREATE TABLE articles (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                content TEXT,
                created_at INTEGER NOT NULL
            )
        """)
        
        // 为现有表添加新列
        database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER")
    }
}

// 在 DatabaseModule 中配置
single {
    Room.databaseBuilder(
        androidContext(),
        AppDatabase::class.java,
        "app_database"
    )
    .addMigrations(MIGRATION_1_2)
    .build()
}
```

**适用场景：**
- 需要保留现有数据
- 需要进行数据转换（例如：合并字段、拆分字段）
- 复杂的表结构变更
- 生产环境的重要数据

---

##### **AutoMigration（推荐用于简单变更）**

**优点：**
- ✅ 自动生成迁移代码
- ✅ 无需编写 SQL
- ✅ 减少人为错误
- ✅ 快速开发迭代

**缺点：**
- ❌ 无法处理复杂的数据转换
- ❌ 某些变更不支持（如重命名列）
- ❌ 可能丢失数据（如删除列）
- ❌ 需要启用 experimental 特性

**使用示例：**
```kotlin
// 1. 在 AppDatabase 中声明 AutoMigration
@Database(
    entities = [UserEntity::class, ArticleEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun articleDao(): ArticleDao
}

// 2. 在 DatabaseModule 中无需额外配置
single {
    Room.databaseBuilder(
        androidContext(),
        AppDatabase::class.java,
        "app_database"
    ).build()  // Room 会自动处理迁移
}
```

**支持的变更：**
- ✅ 添加新表
- ✅ 删除表
- ✅ 添加新列（必须有默认值或可为 null）
- ✅ 删除列
- ✅ 更改列的 nullable 状态

**不支持的变更（需要手动 Migration）：**
- ❌ 重命名表
- ❌ 重命名列
- ❌ 更改列类型（如 String → Int）
- ❌ 更改主键
- ❌ 复杂的数据转换

---

#### 最佳实践

1. **开发阶段**：使用 AutoMigration 快速迭代
   ```kotlin
   @Database(
       entities = [UserEntity::class],
       version = 2,
       autoMigrations = [AutoMigration(from = 1, to = 2)]
   )
   ```

2. **生产环境**：对于重要数据，使用手动 Migration
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // 确保数据安全迁移
           database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER DEFAULT 0")
       }
   }
   ```

3. **混合使用**：结合两种方式
   ```kotlin
   @Database(
       entities = [UserEntity::class, ArticleEntity::class],
       version = 3,
       autoMigrations = [
           AutoMigration(from = 1, to = 2),  // 简单变更用 Auto
           AutoMigration(from = 2, to = 3)
       ]
   )
   abstract class AppDatabase : RoomDatabase() {
       // ...
   }
   
   // 对于复杂变更，仍然可以添加手动 Migration
   single {
       Room.databaseBuilder(...)
           .addMigrations(COMPLEX_MIGRATION_2_3)
           .build()
   }
   ```

4. **始终启用 Schema 导出**
   ```kotlin
   @Database(
       entities = [UserEntity::class],
       version = 1,
       exportSchema = true  // 必须为 true 才能使用 AutoMigration
   )
   ```

5. **测试迁移**
   ```kotlin
   @Test
   fun testMigration1to2() {
       val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation())
       
       // 验证版本 1 的 schema
       var db = helper.createDatabase("test_db", 1)
       // ... 插入测试数据
       db.close()
       
       // 迁移到版本 2
       db = helper.runMigrationsAndValidate("test_db", 2, true, MIGRATION_1_2)
       // ... 验证数据是否正确迁移
   }
   ```

---

#### 常见错误

**错误 1：忘记增加版本号**
```kotlin
// ❌ 错误：修改了 Entity 但没有增加版本号
@Database(entities = [UserEntity::class], version = 1)

// ✅ 正确：增加版本号
@Database(entities = [UserEntity::class], version = 2)
```

**错误 2：没有提供 Migration**
```kotlin
// ❌ 错误：会导致崩溃
Room.databaseBuilder(...).build()

// ✅ 正确：添加 Migration 或使用 fallbackToDestructiveMigration
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()

// ⚠️ 备选方案：破坏性迁移（会丢失所有数据）
Room.databaseBuilder(...)
    .fallbackToDestructiveMigration()
    .build()
```

**错误 3：AutoMigration 不支持的变更**
```kotlin
// ❌ 错误：重命名列不支持 AutoMigration
@Entity(tableName = "users")
data class UserEntity(
    @SerialName("user_name") val userName: String  // 原来是 name
)

// ✅ 正确：使用手动 Migration
val MIGRATION = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE users RENAME COLUMN name TO user_name")
    }
}
```

### 3. 线程安全
- Room 查询自动在主线程外执行
- DAO 中的 suspend 函数确保异步执行
- 返回 Flow 的查询会自动在后台线程运行

### 4. 类型转换
- 复杂类型需要使用 `@TypeConverter`
- 时间统一使用 `kotlinx.datetime.Instant`
- 在 `AppDatabase` 中注册 TypeConverter

### 5. 测试
- 使用内存数据库进行测试：
```kotlin
Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
```

---

## 🧪 测试建议

### 单元测试 DAO

```kotlin
@Test
fun testInsertAndQueryUser() = runTest {
    val database = Room.inMemoryDatabaseBuilder(
        context,
        AppDatabase::class.java
    ).build()
    
    val userDao = database.userDao()
    val user = UserEntity("1", "John", "john@example.com", "")
    
    userDao.insertUser(user)
    
    val result = userDao.getUserStream("1").first()
    assertEquals("John", result?.name)
    
    database.close()
}
```

---

## 📚 相关资源

- [Room 官方文档](https://developer.android.com/training/data-storage/room)
- [Room DAO 查询](https://developer.android.com/training/data-storage/room/accessing-data)
- [Room 数据库迁移](https://developer.android.com/training/data-storage/room/migrating-db-versions)

---

**最后更新：** 2026-05-29
