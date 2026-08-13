# ADR-0012: 接入 MmpKit / AdsKit / PayKit 为 core 基础设施

## 状态

Accepted

---

## 背景

脚手架已按瘦身模式接入 AnalyticsKit（Initializer + 常量 + `api` 透出门面）。需要同样接入归因（MMP）、广告聚合与 Play 支付，作为可选基础设施模块，供 feature 按需依赖。

## 备选方案

### 方案 A：feature 直接依赖各 Kit 的 JitPack 坐标

**缺点：** SDK 细节散落、版本与初始化不统一。

### 方案 B (选定)：`:core:mmp` / `:core:ads` / `:core:pay`

与 `:core:analytics` 一致：

- `api` 依赖对应 Kit，业务直接调用门面（`Mmp` / `AdsManager` / `PayKit`）
- `*Initializer` 集中初始化
- 事件名 / 广告位 / 商品 ID 常量集中管理
- **不**再包一层 Helper / Koin
- **不**写入 `feature.impl` 约定插件自动依赖（按需显式依赖）

## 决策

| 模块 | Kit | 默认实现 / 产物 | 版本 |
|------|-----|-----------------|------|
| `:core:mmp` | MmpKit | `mmp-appsflyer` | v1.0.0 |
| `:core:ads` | AdsKit | `AdsKit-admob`（默认可切 AppLovin 产物） | v1.0.1 |
| `:core:pay` | PayKit | `PayKit` | v1.0.0 |

- 密钥与 App ID 经 `local.properties` → `BuildConfig` / `manifestPlaceholders` 注入。
- MMP `appToken` 为空时跳过初始化（避免无效 Key 启动失败）。
- Ads 默认以 AdMob 初始化；`settings.gradle.kts` 已加入 AdsKit mediation 所需 Maven 仓库。
- App Manifest 声明 AdMob APPLICATION_ID（及可选 AppLovin key）。
- Pay 商品 ID 占位在 `PayProducts`，上线前替换为 Play Console 真实 ID。

## 影响

### 正面影响

- 与 Analytics 接入范式一致，学习成本低
- 广告 / 支付 / 归因体积大，opt-in 模块避免污染全部 feature

### 负面影响

- Ads 全量 / AppLovin 产物体积与仓库更多；默认仅 `AdsKit-admob`，需要时改 catalog 坐标
- AppsFlyer 可能与 Manifest backup 属性冲突，app 已加 `tools:replace`

## 参考

- [MmpKit](https://github.com/e-hai/MmpKit)
- [AdsKit](https://github.com/e-hai/AdsKit)
- [PayKit](https://github.com/e-hai/PayKit)
- ADR-0011（AnalyticsKit）
