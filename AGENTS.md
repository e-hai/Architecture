# Kotlin Multiplatform (KMP) 架构 AI 全局规则 (KMP Architecture Governance)

**角色：** Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP) 移动端跨平台架构守护者，严格维护 `kmp` 分支的工程范式与架构一致性。

---

## 2026 JetBrains 最新项目结构规范 (New Default Project Structure)

本工程全面拥抱 JetBrains 2026 年最新架构与 AGP 9.0 规范，严禁回退到旧单体混杂模式：

| 层次/目录 | 职责与插件 | 依赖规则 |
| :--- | :--- | :--- |
| **`androidApp`** | **独立 Android 宿主** (`com.android.application`)<br/>Application / MainActivity / Manifest / NativeKitBootstrap (8 大 `Android*Delegate`) / 打包 | 依赖 `shared` 库、各 `core` 门面与底层原生 Kit AAR |
| **`shared`** | **纯 KMP 共享应用与 UI 根库** (`myproject.kmp.library` + CMP)<br/>App 根 Composable、AppNavGraph、Koin 装配、导出 `Shared.framework` | 聚合各 `core:*` 与 `feature:*` 模块 |
| **`iosApp`** | **独立 iOS 宿主工程** (Xcode / Swift 6 SPM)<br/>引入 `e-hai/*-iOS` SPM，NativeKitBootstrap (8 大 `Ios*Delegate`)，承载 `MainViewController` | 消费 `shared` 导出的 XCFramework 与 SPM 原生库 |
| **`features:*`** | **业务特性模块** (`api` + `impl`)，CMP 跨平台呈现 | 外部仅依赖 `api`，`impl` 依赖 `core:*` |
| **`core:*`** | **核心基础设施与八大商业化 Kit 门面** (纯 KMP 库) | 单向被上层依赖，严禁反向依赖业务 |

---

## 全平台统一工程核心规范

### 1. 三级模块化分层 (Three-Tier Modularization)
- **`androidApp` / `iosApp`（平台宿主层）**：外置启动入口与薄壳容器，无具体业务逻辑。仅负责系统入口声明、挂载 CMP 根视图，并在 `NativeKitBootstrap` 中提供原生 Kit 的私有 `*Delegate` 实现。
- **`shared` / `features:*`（跨平台业务层）**：跨端业务中枢，承载 CMP 页面、ViewModel、导航状态机分发与全局 Koin 装配。**严禁直接引用任何底层原生 Kit SDK，所有能力必须 100% 通过 `core:*` 门面调用**。
- **`core:*`（核心基础设施层）**：单向被上层依赖，严禁反向依赖业务。细分为「6 大纯 KMP 基础库」与「8 大商业化 Kit 门面契约库」。

### 2. 离线优先策略 (Offline-First)
- 数据层 Repository 统一遵循 Cache-First 范式：先读取 Room KMP / DataStore KMP 本地持久化数据，后通过 Ktor 发起网络请求更新缓存。
- 网络响应 DTO 与本地 Entity 必须在 Repository 内部转换为纯领域模型（Domain Model），禁止原始 DTO 泄露至 UI 层。

### 3. 八大商业化 Kit 跨平台接入铁律 (Commercial Kits)
- **100% 纯 `commonMain` 契约**：8 个商业化 Kit 模块（`log`, `crashreport`, `analytics`, `abtesting`, `mmp`, `ads`, `pay`, `push`）**禁止包含任何 `androidMain` 与 `iosMain` 目录**。仅对外暴露 `Core*` 静态门面、`*Delegate` 接口契约、`Default*Delegate` 兜底实现与业务常量；
- **统一 `Core*` 门面命名**：门面统一规范为 `CoreLog`、`CoreCrashReport`、`CoreAnalytics`、`CoreAbTesting`、`CoreMmp`、`CoreAds`、`CorePay`、`CorePush`，彻底杜绝与底层原生类名冲突（Android 宿主无需 `as` 别名，iOS 宿主无需 `Shared.` 前缀消歧）；
- **双端宿主 1:1 对称委托**：
  - Android 宿主（`androidApp/.../NativeKitBootstrap.kt`）：统一声明 8 个 `private class Android*Delegate : *Delegate`；
  - iOS 宿主（`iosApp/.../NativeKitBootstrap.swift`）：统一声明 8 个 `private final class Ios*Delegate : NSObject, *Delegate`；
- **原生 SDK API 绝对下沉隔离**：全工程中唯一允许 `import` 和调用原生 AAR / SPM API 的地方，严格限制在各自宿主的私有 Delegate 内部；
- **跨端初始化方法统一 `doInit*` 命名**：显式使用 `doInit(...)` / `doInitAllLog()`，规避 Kotlin/Native 导出 Objective-C 头文件时的隐式改名，确保双端调用代码 100% 对齐；
- **契约驱动测试 (Fake over Mock)**：全面禁止在项目中引入动态运行时 Mock 框架，单测通过 `Core*.install(Fake*Delegate())` 注入手写强类型 Fake 实现。

### 4. 双段式冷启动与依赖注入 (Two-Phase Initialization & DI)
- **Phase 1（原生 Kit 宿主层）**：在各自宿主启动入口（`MyApplication.onCreate()` / `iOSApp.init()`）第一步调用 `NativeKitBootstrap.initKits(...)`，完成 8 大原生 SDK 的私有 `*Delegate` 注入与门面初始化；
- **Phase 2（纯 KMP 跨端层）**：在宿主启动入口第二步调用 `AppBootstrap.doInitKoin(...)`，启动纯 KMP 依赖注入容器（Room、DataStore、Ktor），Android 端在此注入 `androidContext`。

### 5. 代码质量与规范
- 所有新增类、函数与方法必须包含详尽的中文注释，阐明其职责与关键逻辑；
- 统一使用 `xxx.yyy.zzz` 作为包名骨架占位符，支持 `init_project.sh` 一键初始化重命名。
