# ADR-0011: 使用 AnalyticsKit 作为统计分析封装

## 状态

Accepted

---

## 背景

脚手架原先在 `:core:analytics` 中手写 Firebase Analytics 封装。引入自研 AnalyticsKit 后，Kit 已提供统一门面与可插拔 Provider，项目侧再包一层 `AnalyticsHelper` / Koin 绑定属于重复抽象。

## 备选方案

### 方案 A：继续维护手写 Firebase 封装

**缺点：** 无法复用 Kit 的 SPI / Composite Provider；与其他 Kit 风格不一致。

### 方案 B：AnalyticsKit + 项目侧 `AnalyticsHelper` + Koin

**缺点：** 与 Kit 门面能力重复；双份 `AnalyticsEvent`；维护噪音大。

### 方案 C (选定)：瘦身 `:core:analytics` = 依赖透出 + 初始化 + 打点常量

- `api(analytics-firebase)`，业务直接使用 `com.kit.analytics.Analytics`
- `AnalyticsInitializer` 集中配置 Provider
- `AnalyticsEvents` / `AnalyticsParams` 统一事件名与参数键，禁止硬编码字符串

## 决策

- 使用 **AnalyticsKit `v1.0.0`**（`analytics-firebase`）。
- **不保留** 项目侧 `AnalyticsHelper` / `AnalyticsKitHelper` / Koin analytics 模块。
- Application 调用 `AnalyticsInitializer.initialize(context, debug)`。
- 跨模块打点关键字放在 `:core:analytics`；Feature 专属事件可在 feature 模块内自建常量。
- Firebase BOM 与 Kit 对齐（34.16.0）；`compileSdk` 对齐 37.1。

## 影响

### 正面影响

- 去掉重复封装，API 与 Kit 文档一致
- 事件名 / 参数键集中，降低拼写与口径不一致风险

### 负面影响

- 业务直接依赖 Kit 门面类型（仍经 `:core:analytics` 传递，不直接写 JitPack 坐标）
- 单元测试需使用 Kit 的 NoOp/Logging Provider，或自行隔离调用点

## 参考

- [AnalyticsKit](https://github.com/e-hai/AnalyticsKit)
- `:core:analytics`
