# ADR-008: 使用约定插件统一 Gradle 配置

## 状态

Accepted

---

## 背景

项目采用多模块架构（app / feature / core），如果每个模块独立配置 Gradle 会导致大量重复代码：所有 Android 库模块都需要设置 compileSdk、minSdk、Java 兼容性；使用 Compose 的模块都需要引入 Compose BOM 和依赖。重复配置不利于统一升级和版本管理。

## 备选方案

- **手动重复配置**：每个模块的 build.gradle.kts 独立编写。维护成本高，升级 SDK 版本需要修改所有模块。
- **Gradle subprojects {} 块**：在根项目统一配置所有子模块。配置隔离性差，无法精细控制不同模块的不同配置。
- **约定插件（build-logic）**：在独立的 included build 中定义可组合的约定插件，模块按需应用。

## 决策

通过 `build-logic` included build 集中管理 Gradle 约定插件。

架构：
- `build-logic/` 作为独立 Gradle 构建被根项目 include
- 插件按职责单一拆分，可组合使用

| 插件 | 职责 |
|------|------|
| `myproject.android.application` | Application 模块配置 |
| `myproject.android.library` | Library 模块基础配置 |
| `myproject.android.feature.api` | Feature API 模块配置 |
| `myproject.android.feature.impl` | Feature Impl 模块配置 |
| `myproject.android.compose` | Compose 支持 |
| `myproject.android.room` | Room + KSP 支持 |
| `myproject.koin` | Koin 依赖注入 |
| `myproject.spotless` | 代码格式化 |

所有第三方依赖版本统一通过 `gradle/libs.versions.toml`（Version Catalog）管理。

## 理由

- 消除跨模块的 Gradle 配置重复
- 版本统一管理，升级时只需修改 Version Catalog 一处
- 新模块创建只需声明应用对应插件，无需手写 SDK 版本和依赖
- 插件可组合，模块精确控制所需能力
- 与 Now in Android 的 Gradle 管理模式一致

## 影响

### 正面影响

- 大量减少 build.gradle.kts 代码量
- 模块配置声明式、自文档化
- 版本统一升级，降低遗漏风险
- 新增模块的配置成本极低

### 负面影响

- 增加了一层抽象，初次接触的开发者需要理解 build-logic 机制
- 插件之间的隐式依赖需要注意应用顺序
- 调试构建问题需要多一层排查（build-logic 代码 → 模块 build.gradle.kts）

### 中立影响

- 插件开发在 build-logic 的 convention 子模块中用 Kotlin 编写
- 共享工具类放在 `build-logic/convention/src/main/kotlin/project/` 包下

---

## 遵循情况

- 所有模块的 build.gradle.kts 必须使用约定插件 alias，禁止手动重复配置
- compileSdk、minSdk、targetSdk 只能定义在 Version Catalog 中
- 新增插件需要先在 convention 模块中注册，再在 Version Catalog 中声明 alias
