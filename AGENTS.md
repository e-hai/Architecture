# Kotlin Multiplatform (KMP) 架构 AI 全局规则 (KMP Architecture Governance)

**角色：** Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP) 移动端跨平台架构守护者，严格维护 `kmp` 分支的工程范式与架构一致性。

---

## 2026 JetBrains 最新项目结构规范 (New Default Project Structure)

本工程全面拥抱 JetBrains 2026 年最新架构与 AGP 9.0 规范，严禁回退到旧单体混杂模式：

| 层次/目录 | 职责与插件 | 依赖规则 |
| :--- | :--- | :--- |
| **`androidApp`** | **独立 Android 宿主** (`com.android.application`)<br/>Application / MainActivity / Manifest / 证书与打包 | 依赖 `shared` 库与各核心初始化入口 |
| **`shared`** | **纯 KMP 共享应用与 UI 根库** (`myproject.kmp.library` + CMP)<br/>App 根 Composable、AppNavGraph、Koin 装配、导出 `Shared.framework` | 聚合各 `core:*` 与 `feature:*` 模块 |
| **`iosApp`** | **独立 iOS 宿主工程** (Xcode / Swift 6 SPM)<br/>引入 `e-hai/*-iOS` SPM，注入 Kit Bridge，承载 `MainViewController` | 消费 `shared` 导出的 XCFramework |
| **`features:*`** | **业务特性模块** (`api` + `impl`)，CMP 跨平台呈现 | 外部仅依赖 `api`，`impl` 依赖 `core:*` |
| **`core:*`** | **核心基础设施与八大商业化 Kit 门面** (纯 KMP 库) | 单向被上层依赖，严禁反向依赖业务 |

---

## 全平台统一工程核心规范

### 1. 三级模块化分层 (Three-Tier Modularization)
- `androidApp` / `iosApp`：平台外置启动入口，无具体业务逻辑。
- `shared`：跨端页面与依赖装配中心。
- `Features:*`：业务特性模块，按需添加，必须拆分为 `api`（仅暴露 `NavKey` 与抽象接口）与 `impl`（具体实现）。
- `Core:*`：核心数据、网络、UI 与 8 大 Kit 门面。

### 2. 离线优先策略 (Offline-First)
- 数据层 Repository 统一遵循 Cache-First 范式：先读取 Room KMP / DataStore KMP 本地持久化数据，后通过 Ktor 发起网络请求更新缓存。
- 网络响应 DTO 与本地 Entity 必须在 Repository 内部转换为纯领域模型（Domain Model），禁止原始 DTO 泄露至 UI 层。

### 3. 商业化 Kit 1:1 全原生透传 (Commercial Kits)
- `commonMain` 统一暴露静态门面与常量（`*Events`、`*Keys`、`*Products`、`*Tags`）；
- `androidMain` 直连 JitPack 上的 `com.github.e-hai.*-Android` 原生 AAR；
- `iosMain` 暴露 `*IosBridge` 接口（内置 Console/NoOp 兜底），由 `iosApp` Swift 宿主挂载 `e-hai/*-iOS` SPM 实现。

### 4. 依赖注入统一管理 (Dependency Injection)
- 统一使用 Koin 进行跨平台依赖注入；
- 通过 `AppBootstrap.doInitKoin` 统一注册，各端在启动时传入平台特定配置（如 Android `androidContext`）。

### 5. 测试规范 (Fake over Mock)
- 全面禁止在项目中引入动态运行时 Mock 框架；
- 必须使用手写强类型 Fake 实现（如 `FakeNetworkDataSource`、`FakeKeyValueStorage`）进行单元测试。

### 6. 代码质量与规范
- 所有新增类、函数与方法必须包含详尽的中文注释，阐明其职责与关键逻辑；
- 统一使用 `xxx.yyy.zzz` 作为包名骨架占位符，支持 `init_project.sh` 一键初始化重命名。
