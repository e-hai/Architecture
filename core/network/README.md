# Core Network 模块

网络层模块，提供 Retrofit、OkHttp 和序列化配置，封装 Remote DataSource。

---

## 📦 依赖

- `:core:model` — 领域模型（DTO 通过 Mapper 转换为领域模型）
- `libs.retrofit-core` — Retrofit 网络框架
- `libs.retrofit-kotlin-serialization` — kotlinx-serialization 转换器
- `libs.okhttp-logging` — OkHttp 日志拦截器
- `libs.kotlinx-serialization-json` — JSON 序列化

---

## 📦 公共 API

| 类型 | 说明 |
|------|------|
| `UserResponse` | 网络响应 DTO（位于 `model/` 目录，以 `Response` 结尾） |
| `UserService` | Retrofit 服务接口 |
| `object coreNetworkModule` | Koin 模块，提供 OkHttpClient、Retrofit 和 Service 实例 |

---

## 📦 模块结构

```
core/network/
├── src/main/java/xxx/yyy/zzz/core/network/
│   ├── model/                    # 网络响应 DTO
│   │   └── UserResponse.kt
│   ├── UserService.kt            # Retrofit API 接口
│   └── NetworkModule.kt          # Koin 依赖注入模块
├── build.gradle.kts
└── README.md                     # 本文件
```

---

## 🏗️ 目录结构规范

### `model/` 文件夹
存放所有网络响应的 DTO（Data Transfer Object）类。

**命名规范：**
- **必须以 `Response` 作为类名后缀**（例如：`UserResponse`、`ArticleResponse`）
- 文件名与类名保持一致（例如：`UserResponse.kt`）

**示例：**
```kotlin
package xxx.yyy.zzz.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xxx.yyy.zzz.core.model.User

@Serializable
data class UserResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("avatar_url") val avatarUrl: String
) {
    fun toDomainModel(): User = User(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl
    )
}
```

**规范：**
- 使用 `@Serializable` 注解支持 Kotlinx Serialization
- 使用 `@SerialName` 映射后端字段名（snake_case）到 Kotlin 属性名（camelCase）
- 提供 `toDomainModel()` 方法转换为领域模型
- **禁止**在模块间直接传递 DTO，必须通过 Mapper 转换

### 根目录文件

#### `UserService.kt`
Retrofit API 接口定义。

```kotlin
package xxx.yyy.zzz.core.network

import retrofit2.http.GET
import retrofit2.http.Path
import xxx.yyy.zzz.core.network.model.UserResponse

interface UserService {
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): UserResponse
}
```

**规范：**
- 每个业务域对应一个 Service 接口
- 所有方法使用 `suspend` 修饰符
- 返回 DTO 对象或列表
- 使用标准的 Retrofit 注解（@GET, @POST, @PUT, @DELETE 等）

#### `NetworkModule.kt`
Koin 依赖注入模块，配置 Retrofit 和 OkHttp。

```kotlin
package xxx.yyy.zzz.core.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val coreNetworkModule = module {
    // HTTP 日志拦截器
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // OkHttp 客户端
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    // Retrofit 实例
    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .client(get<OkHttpClient>())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // API Service
    single {
        get<Retrofit>().create(UserService::class.java)
    }
}
```

---

## 💡 使用示例

### 在 Repository 中使用

```kotlin
class UserRepositoryImpl(
    private val userService: UserService,
    private val userDao: UserDao,
    private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    
    override suspend fun refreshUser(userId: String) {
        withContext(ioDispatcher) {
            try {
                // 从网络获取数据
                val userResponse = userService.getUser(userId)
                
                // 转换为 Entity 并保存到数据库
                userDao.insertUser(userResponse.toEntity())
            } catch (e: Exception) {
                // 处理网络错误
                throw RuntimeException("Failed to fetch user", e)
            }
        }
    }
}
```

### 添加新的 API

1. **在 `model/` 中创建 DTO：**
```kotlin
// model/ArticleResponse.kt
package xxx.yyy.zzz.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long
) {
    fun toDomainModel(): Article = Article(
        id = id,
        title = title,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(createdAt)
    )
}
```

2. **创建 Service 接口：**
```kotlin
// ArticleService.kt
package xxx.yyy.zzz.core.network

import retrofit2.http.GET
import retrofit2.http.Path
import xxx.yyy.zzz.core.network.model.ArticleResponse

interface ArticleService {
    @GET("articles/{articleId}")
    suspend fun getArticle(@Path("articleId") articleId: String): ArticleResponse
    
    @GET("articles")
    suspend fun getAllArticles(): List<ArticleResponse>
}
```

3. **在 `NetworkModule` 中注册：**
```kotlin
val coreNetworkModule = module {
    // ... 现有配置
    
    // 添加新的 Service
    single {
        get<Retrofit>().create(ArticleService::class.java)
    }
}
```

---

## ⚠️ 注意事项

### 1. Base URL 配置
- 不要在代码中硬编码 Base URL
- 使用 BuildConfig 或 gradle.properties 配置不同环境的 URL
- 示例：
```kotlin
.baseUrl(BuildConfig.API_BASE_URL)
```

### 2. 错误处理
- 使用 try-catch 捕获网络异常
- 区分不同类型的错误（网络错误、服务器错误、解析错误）
- 返回 sealed class Result 或使用 Kotlin Result

### 3. 超时配置
```kotlin
OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

### 4. 认证拦截器
```kotlin
val authInterceptor = Interceptor { chain ->
    val token = getToken() // 从 DataStore 或其他地方获取
    val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    chain.proceed(request)
}

OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .build()
```

### 5. JSON 配置
```kotlin
val json = Json {
    ignoreUnknownKeys = true      // 忽略未知字段
    isLenient = true              // 宽松模式
    encodeDefaults = false        // 不编码默认值
    explicitNulls = false         // 不编码 null 值
}
```

### 6. 线程安全
- Retrofit 的 suspend 函数自动在后台线程执行
- 不要在主线程调用网络请求
- Repository 中使用 `withContext(ioDispatcher)` 确保线程安全

---

## 🧪 测试建议

### 单元测试 Service

使用 MockWebServer 进行网络测试：

```kotlin
@Test
fun testGetUser() = runTest {
    val mockWebServer = MockWebServer()
    mockWebServer.start()
    
    val baseUrl = mockWebServer.url("/").toString()
    
    // 准备模拟响应
    val jsonResponse = """
        {
            "id": "1",
            "name": "John Doe",
            "email": "john@example.com",
            "avatar_url": "https://example.com/avatar.jpg"
        }
    """.trimIndent()
    
    mockWebServer.enqueue(
        MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
    )
    
    // 创建 Retrofit 实例
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    val userService = retrofit.create(UserService::class.java)
    
    // 执行请求
    val response = userService.getUser("1")
    
    // 验证结果
    assertEquals("1", response.id)
    assertEquals("John Doe", response.name)
    
    mockWebServer.shutdown()
}
```

---

## 📚 相关资源

- [Retrofit 官方文档](https://square.github.io/retrofit/)
- [OkHttp 官方文档](https://square.github.io/okhttp/)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)

---

**最后更新：** 2026-05-29
