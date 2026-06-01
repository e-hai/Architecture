# Build-Logic 约定插件

Gradle 约定插件集中管理中心，提供统一的模块配置和依赖管理。

---

## 📦 项目结构

```
build-logic/
├── convention/
│   ├── src/main/kotlin/
│   │   ├── project/                    # 共享工具类
│   │   │   ├── KotlinAndroid.kt        # Android 配置函数
│   │   │   ├── ProjectExtensions.kt    # Project 扩展属性
│   │   │   ├── ProjectFlavor.kt        # 产品变体（Flavor）配置
│   │   │   └── ProjectBuildType.kt     # 构建类型（Build Type）配置
│   │   ├── AndroidApplicationConventionPlugin.kt
│   │   ├── AndroidLibraryConventionPlugin.kt
│   │   ├── AndroidFeatureApiConventionPlugin.kt
│   │   ├── AndroidFeatureImplConventionPlugin.kt
│   │   ├── AndroidComposeConventionPlugin.kt
│   │   ├── AndroidRoomConventionPlugin.kt
│   │   ├── KoinConventionPlugin.kt
│   │   └── SpotlessConventionPlugin.kt
│   └── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🔌 可用插件

### 1. myproject.android.application

**用途：** 配置 Application 模块

**自动应用：**
- `com.android.application`
- `myproject.android.compose`
- `myproject.spotless`

**配置内容：**
- compileSdk、minSdk、targetSdk（从 Version Catalog 读取）
- Java 17 兼容性
- Compose 支持
- Spotless 代码格式化

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.application)
}

android {
    namespace = "xxx.yyy.zzz"
    
    defaultConfig {
        applicationId = "xxx.yyy.zzz"
        versionCode = 1
        versionName = "1.0"
    }
}
```

---

### 2. myproject.android.library

**用途：** 配置 Android Library 模块

**自动应用：**
- `com.android.library`
- `myproject.spotless`

**配置内容：**
- compileSdk、minSdk（从 Version Catalog 读取）
- Java 17 兼容性
- consumerProguardFiles 配置
- Spotless 代码格式化

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    namespace = "xxx.yyy.zzz.core.data"
}
```

---

### 3. myproject.android.feature.api

**用途：** 配置 Feature API 模块（对外暴露接口）

**自动应用：**
- `myproject.android.library`
- `org.jetbrains.kotlin.plugin.serialization`

**自动依赖：**
- `androidx-navigation3-runtime`
- `kotlinx-serialization-json`

**配置内容：**
- 所有 Library 插件的配置
- Kotlin Serialization 支持
- Navigation3 运行时依赖

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.feature.api)
}

android {
    namespace = "xxx.yyy.zzz.feature.home.api"
}

// 无需手动添加 navigation 和 serialization 依赖
```

---

### 4. myproject.android.feature.impl

**用途：** 配置 Feature Impl 模块（实现 UI 和 ViewModel）

**自动应用：**
- `myproject.android.library`
- `myproject.android.compose`
- `myproject.koin`

**自动依赖：**
- `:core:ui`
- `:core:navigation`

**配置内容：**
- 所有 Library 插件的配置
- Compose 支持
- Koin 依赖注入
- 核心 UI 和导航模块

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.feature.impl)
}

android {
    namespace = "xxx.yyy.zzz.feature.home.impl"
}

dependencies {
    // 只需添加业务相关的依赖
    implementation(project(":feature:home:api"))
    implementation(project(":core:data"))
}
```

---

### 5. myproject.android.compose

**用途：** 为模块添加 Compose 支持

**自动应用：**
- `org.jetbrains.kotlin.plugin.compose`

**自动依赖：**
- Compose BOM
- compose-ui
- compose-ui-graphics
- compose-ui-tooling-preview
- compose-material3
- compose-material-icons-extended
- compose-foundation
- lifecycle-runtime-compose
- lifecycle-viewmodel-compose
- debugImplementation: compose-ui-tooling
- debugImplementation: compose-ui-test-manifest

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.compose)
}
```

---

### 6. myproject.android.room

**用途：** 配置 Room 数据库模块

**自动应用：**
- `androidx.room`
- `com.google.devtools.ksp`

**自动依赖：**
- room-runtime
- room-ktx
- ksp: room-compiler

**配置内容：**
- Schema 导出目录：`$projectDir/schemas`

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.room)
}

android {
    namespace = "xxx.yyy.zzz.core.database"
}

// 无需手动配置 Room 和 KSP
```

---

### 7. myproject.koin

**用途：** 配置 Koin 依赖注入

**智能依赖：**
- 基础：`koin-core`
- 如果应用了 `com.android.library` 或 `com.android.application`：添加 `koin-android`
- 如果应用了 Compose 插件：添加 `koin-androidx-compose`

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.koin)
}

// 自动添加 koin-core 和 koin-android
```

---

### 8. myproject.spotless

**用途：** 配置 Spotless 代码格式化

**自动应用：**
- `com.diffplug.spotless`

**配置内容：**
- Kotlin 文件：使用 ktlint
- Gradle Kotlin DSL 文件：使用 ktlint
- 排除 build 目录

**使用示例：**
```kotlin
plugins {
    alias(libs.plugins.myproject.spotless)
}

// 运行格式化：./gradlew spotlessApply
// 检查格式：./gradlew spotlessCheck
```

---

## 🛠️ 共享工具类

### KotlinAndroid.kt

提供统一的 Android 配置函数：

```kotlin
fun Project.configureKotlinAndroid(commonExtension: Any)
```

**配置内容：**
- compileSdk、minSdk（从 Version Catalog 读取）
- Java 17 兼容性
- Kotlin 编译器选项：
  - JVM target: 17
  - Opt-in: ExperimentalCoroutinesApi
  - Opt-in: OptIn

---

### ProjectExtensions.kt

提供 Project 扩展属性：

```kotlin
val Project.libsCatalog: VersionCatalog
```

**用途：** 在约定插件中访问 Version Catalog

---

### ProjectFlavor.kt

提供产品变体（Flavor）配置工具和枚举定义。

#### FlavorDimension 枚举

定义产品变体的维度：

```kotlin
enum class FlavorDimension {
    contentType,  // 内容类型维度
}
```

#### ProjectFlavor 枚举

定义具体的产品变体：

```kotlin
enum class ProjectFlavor(
    val dimension: FlavorDimension,
    val applicationIdSuffix: String? = null
) {
    dev(FlavorDimension.contentType, applicationIdSuffix = ".dev"),  // 开发测试版
    prod(FlavorDimension.contentType),  // 生产发布版
}
```

**可用变体：**
- **dev**: 开发测试版
  - Application ID 后缀: `.dev`
  - 使用独立的测试环境
  - 适用于内部测试和开发调试
  
- **prod**: 生产发布版
  - 无 Application ID 后缀
  - 使用线上正式环境
  - 适用于正式发布到应用商店

#### configureFlavors 函数

自动配置所有产品变体：

```kotlin
fun configureFlavors(
    commonExtension: CommonExtension,
    flavorConfigurationBlock: ProductFlavor.(flavor: ProjectFlavor) -> Unit = {}
)
```

**使用示例：**

```kotlin
// 在 AndroidApplicationConventionPlugin 中使用
extensions.configure<ApplicationExtension> {
    configureKotlinAndroid(this)
    
    // 配置产品变体
    configureFlavors(this) { flavor ->
        when (flavor) {
            ProjectFlavor.dev -> {
                versionNameSuffix = "-DEV"
                buildConfigField("String", "API_URL", "\"https://api.dev.example.com\"")
            }
            ProjectFlavor.prod -> {
                versionNameSuffix = "-PROD"
                buildConfigField("String", "API_URL", "\"https://api.example.com\"")
            }
        }
    }
}
```

**生成的构建变体：**
- `devDebug`: 开发版 + 调试构建
- `devRelease`: 开发版 + 发布构建
- `prodDebug`: 生产版 + 调试构建
- `prodRelease`: 生产版 + 发布构建

---

### ProjectBuildType.kt

提供构建类型（Build Type）配置枚举。

#### ProjectBuildType 枚举

定义应用的构建类型：

```kotlin
enum class ProjectBuildType(val applicationIdSuffix: String? = null) {
    DEBUG(".debug"),  // 调试构建
    RELEASE,          // 发布构建
}
```

**可用构建类型：**
- **DEBUG**: 调试构建类型
  - Application ID 后缀: `.debug`
  - 启用调试功能（日志、调试符号）
  - 适用于开发阶段的测试和调试
  - 可与发布版同时安装在同一设备上
  
- **RELEASE**: 发布构建类型
  - 无 Application ID 后缀
  - 启用代码优化和混淆（R8/ProGuard）
  - 禁用调试功能
  - 适用于正式发布到应用商店

**使用示例：**

```kotlin
// 在 build.gradle.kts 中引用
import project.ProjectBuildType

android {
    buildTypes {
        getByName(ProjectBuildType.DEBUG.name.lowercase()) {
            applicationIdSuffix = ProjectBuildType.DEBUG.applicationIdSuffix
            isDebuggable = true
        }
        
        getByName(ProjectBuildType.RELEASE.name.lowercase()) {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

**注意：** 此配置在 `:app` 和 `:benchmarks` 模块之间共享，提供类型安全的配置。

---

## 📋 插件组合示例

### App 模块
```kotlin
plugins {
    alias(libs.plugins.myproject.android.application)
    alias(libs.plugins.myproject.koin)
}
```

**结果：** Application + Compose + Spotless + Koin

---

### Core Library 模块
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.koin)
}
```

**结果：** Library + Spotless + Koin

---

### Feature API 模块
```kotlin
plugins {
    alias(libs.plugins.myproject.android.feature.api)
}
```

**结果：** Library + Serialization + Navigation3 + Spotless

---

### Feature Impl 模块
```kotlin
plugins {
    alias(libs.plugins.myproject.android.feature.impl)
}
```

**结果：** Library + Compose + Koin + core:ui + core:navigation + Spotless

---

### Database 模块
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.room)
    alias(libs.plugins.myproject.koin)
}
```

**结果：** Library + Room + KSP + Koin + Spotless

---

## ⚙️ 配置原理

### 1. Version Catalog 访问

通过 `ProjectExtensions.kt` 提供统一的访问方式：

```kotlin
val Project.libsCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
```

### 2. 插件注册

在 `convention/build.gradle.kts` 中使用 `register()` 方式：

```kotlin
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "myproject.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
```

### 3. 条件依赖

Koin 插件根据已应用的插件智能添加依赖：

```kotlin
pluginManager.withPlugin("com.android.library") {
    add("implementation", libsCatalog.findLibrary("koin-android").get())
}
```

---

## 🎯 最佳实践

### 1. 不要重复配置

❌ **错误：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
}

android {
    compileSdk = 37
    minSdk = 26
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
```

✅ **正确：**
```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.compose)
}

android {
    namespace = "xxx.yyy.zzz.core.ui"
}
```

### 2. 选择合适的插件

- Application 模块 → `myproject.android.application`
- Library 模块 → `myproject.android.library`
- Feature API → `myproject.android.feature.api`
- Feature Impl → `myproject.android.feature.impl`
- Database → `myproject.android.room`

### 3. 插件可组合

可以组合多个插件：

```kotlin
plugins {
    alias(libs.plugins.myproject.android.library)
    alias(libs.plugins.myproject.android.compose)
    alias(libs.plugins.myproject.koin)
    alias(libs.plugins.myproject.spotless)
}
```

---

## 🔧 开发新插件

### 步骤 1：创建插件类

```kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyCustomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 应用其他插件
            pluginManager.apply("some.plugin")
            
            // 配置扩展
            extensions.configure<SomeExtension> {
                // 配置内容
            }
            
            // 添加依赖
            dependencies {
                add("implementation", libsCatalog.findLibrary("some-lib").get())
            }
        }
    }
}
```

### 步骤 2：注册插件

在 `convention/build.gradle.kts` 中添加：

```kotlin
gradlePlugin {
    plugins {
        register("myCustom") {
            id = "myproject.custom"
            implementationClass = "MyCustomConventionPlugin"
        }
    }
}
```

### 步骤 3：使用插件

```kotlin
plugins {
    alias(libs.plugins.myproject.custom)
}
```

---

## 📚 相关资源

- [Gradle 约定插件文档](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [Now in Android build-logic](https://github.com/android/nowinandroid/tree/main/build-logic)
- [Gradle Version Catalog](https://docs.gradle.org/current/userguide/platforms.html)

---

**最后更新：** 2026-05-29  
**维护者：** 项目架构团队
