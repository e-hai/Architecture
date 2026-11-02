# 多平台移动架构 AI 全局规则 (Multi-Platform Architecture Governance)

**角色：** 移动端全平台架构守护者，严格维护 `main`、`android`、`ios`、`kmp` 各分支的工程范式与架构一致性。

---

## 仓库分支架构与维护规范

本仓库采用多分支隔离的现代化多平台架构布局：

| 分支 | 定位 | 技术基准 | 独立性 |
| :--- | :--- | :--- | :--- |
| **`main`** | **多平台架构总览与导航门户** | Markdown / 文档 / 选型矩阵 | 无业务代码，只维护全局中枢与对比规范 |
| **`android`** | **Android 现代原生架构基线** | Jetpack Compose + Koin + Room + Kit 门面 | 独立的 Android Gradle 工程骨架 |
| **`ios`** | **iOS 现代原生架构基线** | SwiftUI + Swift 6 并发 + SPM + StoreKit 2 | 独立的 iOS SPM / Xcode 工程骨架 |
| **`kmp`** | **Kotlin Multiplatform 跨端架构基线** | CMP + Ktor + Room KMP + Multiplatform Settings | 独立的跨平台多端工程骨架 |

### 分支维护与修改准则
1. **主分支纯洁性**：
   - `main` 分支严禁直接提交单一平台的实现代码（如 Gradle 脚本或 Swift 源码）；
   - `main` 专注于维护多端架构演进对比、选型矩阵、全局设计原则与分支导航。
2. **跨平台设计对称性**：
   - 当在某一平台引入新的通用基础设施（例如新增 `TraceKit` 链路追踪或新的广告形式）时，必须在各个分支提供对称的抽象门面与使用体验；
   - 命名概念统一：如 `AnalyticsKit`、`AdsKit`、`PayKit`、`PushKit`、`LogKit`、`AbTestingKit`、`CrashReportKit` 在所有端保持一致。

---

## 全平台统一工程核心规范

无论是 Android、iOS 还是 KMP，必须严格遵循以下五大铁律：

### 1. 三级模块化分层 (Three-Tier Modularization)
- `App`：主入口与依赖装配中心，**无具体业务代码**。
- `Features:*`：业务特性模块，按需添加。**必须严格拆分为 `Interface` 与 `Impl`**，外部仅依赖 `Interface`。
- `Core:*`：核心基础设施层（Model、Network、Storage、UI、Kits），单向被上层依赖。

### 2. 离线优先策略 (Offline-First)
- 数据层 Repository 统一遵循 Cache-First 范式：先读取本地持久化数据呈现，后并发发起网络请求更新缓存；
- 网络响应 DTO 与本地 Entity 必须在 Repository 内部转换为纯领域模型（Domain Model），禁止原始 DTO 泄露至 UI 层。

### 3. 商业化 Kit 门面解耦 (Commercial Kits)
- 严禁在业务 Feature 中直接调用具体的第三方 SDK（如直接调用 Firebase / AppsFlyer / AdMob / StoreKit 原生类）；
- 必须通过 `Core` 暴露的 Kit 门面调用，确保第三方供应商可无感插拔替换。

### 4. 依赖注入统一管理 (Dependency Injection)
- Android / KMP 统一使用 Koin；
- iOS 统一使用原生轻量容器 `CoreDI` 与 `@Injected`；
- 所有跨层服务必须基于抽象协议（Interface / Protocol）进行注册与解析。

### 5. 测试规范 (Fake over Mock)
- 全面禁止在项目中引入动态运行时 Mock 框架；
- 必须使用手写强类型 Fake 实现（如 `FakeNetworkClient`、`FakeStorage`）进行单元测试与集成测试。

---

## 架构决策记录 (ADR) 规范

- 各分支的重大技术选型与架构变更均需在各自分支的 `docs/adr/` 目录下以 MADR 格式记录。
- 状态流转：`Proposed` → `Accepted` → `Deprecated` / `Superseded`。
- 编号递增，使用中文详细阐述背景、驱动因素、考虑选项与正反面影响。
