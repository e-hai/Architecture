# ADR-0013: 接入 PushKit 为 core 推送 / 通知基础设施

## 状态

Accepted

---

## 背景

需要在脚手架中统一本地通知（渠道、权限、DSL）与 FCM 等厂商推送抽象，避免各 feature 直接依赖系统 Notification API 或各自接入 FCM。

## 决策

新增 `:core:push`，接入方式与 Analytics / MMP 等瘦身模式一致：

- `api("com.github.e-hai:PushKit:v1.0.0")`，业务直接使用 `PushKitManager` / `notifyPush`
- `PushInitializer`：创建默认渠道 + 可选注册 `FcmPushProvider`
- `PushChannels` / `PushNotificationIds`：渠道与本地通知 ID 常量
- **不**再包 Helper / Koin；**不**写入 `feature.impl` 自动依赖

Kit 库 Manifest 已声明 `PushKitFirebaseMessagingService` 与 `POST_NOTIFICATIONS`，随 `api` 依赖合并进 app。

## 影响

- 依赖 Firebase Messaging（与现有 Google Services / Firebase BOM 共存）
- Android 13+ 仍需在合适时机请求运行时通知权限（可用 Kit 的 `hasNotificationPermission` / 系统设置跳转）

## 参考

- [PushKit](https://github.com/e-hai/PushKit)
- ADR-0011 / ADR-0012
