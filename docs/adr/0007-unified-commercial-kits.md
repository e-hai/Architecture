# 7. 统一商业化与基础设施门面体系 (Commercial Kits)

- 状态: Accepted
- 日期: 2026-11-02
- 决策者: iOS 架构团队

## 背景与问题陈述

商业化应用涉及众多第三方 SDK（如 Firebase Analytics、Google AdMob、StoreKit 支付、推送 APNs、日志与崩溃统计）。如果业务代码直接导入第三方 SDK，会导致强耦合且升级替换困难。

## 决策驱动因素

- 业务层与具体第三方 SDK 供应商解耦
- 统一参数命名规范与事件管理中心
- 与 Android 架构的 Kit 体系（AnalyticsKit, AdsKit, PayKit 等）保持概念对齐

## 决策结果

选定选项：
- 在 `Core*` 各模块分别暴露门面（Facade）：`CoreAnalytics`, `CoreAds`, `CorePay`, `CorePush`, `CoreLog`, `CoreAbTesting`, `CoreCrashReport`；
- 业务代码仅直接面向门面 API 编程，底层 Provider 可无感插拔与替换。
