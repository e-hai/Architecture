# Core Analytics 模块

Firebase Analytics 封装模块，提供统一的分析事件追踪接口。

---

## 📦 依赖

- `libs.firebase-bom` — Firebase BOM 版本管理
- `libs.firebase-analytics` — Firebase Analytics SDK
- `libs.koin-core`, `libs.koin-android` — Koin 依赖注入

---

## 📦 公共 API

| 类型 | 说明 |
|------|------|
| `data class AnalyticsEvent(name: String, params: Map<String, String>)` | 分析事件数据类 |
| `interface AnalyticsHelper` | 分析助手接口，定义统一的事件追踪方法 |
| `class FirebaseAnalyticsHelper` | Firebase 实现 |
| `object coreAnalyticsModule` | Koin 模块，绑定 `AnalyticsHelper` |

---

## 📦 模块结构

```
core/analytics/
├── src/main/java/xxx/yyy/zzz/core/analytics/
│   ├── AnalyticsEvent.kt          # 分析事件数据类
│   ├── AnalyticsHelper.kt         # 分析助手接口
│   ├── FirebaseAnalyticsHelper.kt # Firebase 实现
│   └── AnalyticsModule.kt         # Koin 依赖注入模块
├── build.gradle.kts
└── README.md                      # 本文件
```

---

## 🔥 Firebase 配置指南

### 1. 在 Firebase Console 中注册应用

1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 创建新项目或选择现有项目
3. 点击"添加应用" → 选择 Android
4. 输入应用包名（例如：`xxx.yyy.zzz`）
5. 下载 `google-services.json` 文件

### 2. 放置配置文件

将下载的 `google-services.json` 文件放置在：
```
app/google-services.json
```

### 3. 配置 Gradle 插件

#### 根目录 `build.gradle.kts`
```kotlin
plugins {
    // ... 其他插件
    alias(libs.plugins.google.services) apply false
}
```

#### App 模块 `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.myproject.android.application)
    alias(libs.plugins.myproject.koin)
    alias(libs.plugins.google.services)  // 添加此行
}

dependencies {
    // Firebase BOM for version management
    implementation(platform(libs.firebase.bom))
    // ... 其他依赖
}
```

#### Analytics 模块 `core/analytics/build.gradle.kts`
```kotlin
dependencies {
    // Firebase BOM for version management
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
```

### 4. 初始化 Firebase

在 `MyApplication.kt` 的 `onCreate()` 中初始化：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Firebase
        FirebaseApp.initializeApp(this)
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                coreAnalyticsModule,
                // ... 其他模块
            )
        }
    }
}
```

---

## 💡 使用示例

### 记录事件

```kotlin
class MyViewModel(
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    
    fun onButtonClick() {
        // 记录自定义事件
        analyticsHelper.logEvent(
            AnalyticsEvent(
                name = "button_click",
                params = mapOf(
                    "button_name" to "submit",
                    "screen" to "home"
                )
            )
        )
    }
}
```

### 设置用户属性

```kotlin
analyticsHelper.setUserProperty("user_type", "premium")
analyticsHelper.setUserId("user_12345")
```

### 启用/禁用分析

```kotlin
// 用户同意隐私政策后启用
analyticsHelper.setAnalyticsCollectionEnabled(true)

// 用户退出时禁用
analyticsHelper.setAnalyticsCollectionEnabled(false)
```

---

## 🏗️ 架构设计

### 核心组件

#### AnalyticsHelper 接口
```kotlin
interface AnalyticsHelper {
    fun logEvent(event: AnalyticsEvent)
    fun setUserProperty(name: String, value: String?)
    fun setUserId(userId: String?)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
```

#### FirebaseAnalyticsHelper 实现
- 使用 `lazy` 延迟初始化 Firebase Analytics
- 在使用前检查 FirebaseApp 是否已初始化
- 通过 Koin 注入 Context

```kotlin
class FirebaseAnalyticsHelper(private val context: Context) : AnalyticsHelper {
    private val firebaseAnalytics by lazy {
        // 确保 Firebase 已初始化
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        Firebase.analytics
    }
    
    // ... 实现方法
}
```

#### Koin 模块
```kotlin
val coreAnalyticsModule = module {
    single<AnalyticsHelper> {
        FirebaseAnalyticsHelper(androidContext())
    }
}
```

---

## ⚠️ 注意事项

### 1. 初始化顺序
- **必须**在任何 Firebase 服务使用前调用 `FirebaseApp.initializeApp()`
- 建议在 Application 的 `onCreate()` 中尽早初始化

### 2. 延迟初始化
- `FirebaseAnalyticsHelper` 使用 `lazy` 延迟初始化
- 确保在实际使用时 Firebase 已经完成初始化

### 3. 安全检查
- 模块内部会检查 FirebaseApp 是否已初始化
- 如果未初始化，会自动尝试初始化（但最好在主应用中显式初始化）

### 4. 版本管理
- 使用 Firebase BOM 统一管理所有 Firebase 库的版本
- 不要单独指定 Firebase 库的版本号

### 5. ProGuard/R8 混淆
- 确保在 `proguard-rules.pro` 中添加必要的保留规则
- Library 模块已通过 `consumerProguardFiles` 配置了默认规则

---

## 🧪 测试建议

### 单元测试
由于 Firebase Analytics 需要真实环境，建议使用 Fake 实现进行测试：

```kotlin
class FakeAnalyticsHelper : AnalyticsHelper {
    val loggedEvents = mutableListOf<AnalyticsEvent>()
    
    override fun logEvent(event: AnalyticsEvent) {
        loggedEvents.add(event)
    }
    
    override fun setUserProperty(name: String, value: String?) {
        // no-op
    }
    
    override fun setUserId(userId: String?) {
        // no-op
    }
    
    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        // no-op
    }
}
```

### UI 测试
在 UI 测试中可以使用相同的 Fake 实现来验证事件是否正确记录。

---

## 📚 相关资源

- [Firebase Analytics 官方文档](https://firebase.google.com/docs/analytics)
- [Firebase Android 设置指南](https://firebase.google.com/docs/android/setup)
- [Google Services 插件文档](https://developers.google.com/android/guides/google-services-plugin)

---

**最后更新：** 2026-05-29
