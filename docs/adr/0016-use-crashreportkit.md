# ADR-0016: 使用 CrashReportKit 作为崩溃上报封装

## 状态

Accepted

---

## 背景

`:core:crashreport` 原先手写 `CrashReportHelper` + Firebase Crashlytics + Koin。已有自研 [CrashReportKit](https://github.com/e-hai/CrashReportKit) 提供 `CrashReport` 门面与可替换后端，继续维护项目内双重封装与其它 Kit 风格不一致。

## 决策

- 使用 **CrashReportKit `v1.0.0`**（JitPack 坐标 `com.github.e-hai:CrashReportKit`）。
- 瘦身 `:core:crashreport`：`api` 透出门面 + `CrashReportInitializer` + `CrashReportKeys`。
- **删除** `CrashReportHelper` / `FirebaseCrashReportHelper` / `coreCrashReportModule`。
- Application 在 Log 之后尽早调用 `CrashReportInitializer`；业务直接使用 `com.kit.crashreport.CrashReport`。
- 宿主继续由约定插件应用 `google-services` 与 `firebase-crashlytics`；真实上报需替换占位 `google-services.json`。

## 参考

- [CrashReportKit](https://github.com/e-hai/CrashReportKit)
- ADR-0011（AnalyticsKit 瘦身范式）
