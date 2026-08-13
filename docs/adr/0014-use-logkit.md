# ADR-0014: 接入 LogKit 为 core 日志基础设施

## 状态

Accepted

---

## 背景

脚手架需要统一 Logcat、可选磁盘持久化与应用内调试控制台，避免各模块直接使用 `android.util.Log` 且无法统一筛选 / 落盘。

## 决策

新增 `:core:log`，接入方式与其它 Kit 瘦身模式一致：

- `api("com.github.e-hai.LogKit:log:v1.0.0")`，业务直接调用 `LogKit`
- `LogInitializer`：Debug 使用 `initAllLog`（Logcat + 磁盘 + `showLogUi`），Release 使用 `initOnlyAndroidLog`
- `LogTags`：集中管理跨模块 Tag 常量
- **不**再包 Helper / Koin；**不**写入 `feature.impl` 自动依赖
- Application 中 **最先**初始化 Log，便于后续 Kit 启动日志可用

唤起控制台：`LogKit.showLogUi(activity)`（建议仅 Debug 入口暴露）。

## 参考

- [LogKit](https://github.com/e-hai/LogKit)
- ADR-0011 / ADR-0012 / ADR-0013
