# Module: :feature:settings:api

Settings 功能模块的公共 API 模块。只暴露导航键，**不含任何 UI 或业务实现**。

---

## 依赖

- 无模块间依赖（`myproject.android.feature.api` 约定插件自动引入 Navigation3 + Serialization）

---

## 目录结构

```
feature/settings/api/
├── src/main/java/xxx/yyy/zzz/feature/settings/api/
│   └── SettingsNavKey.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `@Serializable data object SettingsNavKey` | 设置主页导航键 |
| `@Serializable data object UserAgreementNavKey` | 用户协议页导航键 |
| `@Serializable data object PrivacyPolicyNavKey` | 隐私政策页导航键 |
| `@Serializable data object AboutAppNavKey` | 关于应用页导航键 |

---

## 使用示例

```kotlin
// 从其他模块导航到设置页面
navigator.navigate(SettingsNavKey)

// 导航到用户协议页面
navigator.navigate(UserAgreementNavKey)
```

---

## 测试

纯导航键对象，无需测试。

---

> **最后更新：** 2026-06-25
