# Module: :feature:settings:impl

Settings 功能模块的实现模块。包含设置主页、用户协议、隐私政策、关于应用四个页面，以及 ViewModel、导航注册和 Koin 模块。

---

## 依赖

- `:feature:settings:api` — 导航键定义
- `:feature:home:api` — HomeNavKey（设置页可跳转到主页）

---

## 目录结构

```
feature/settings/impl/
├── src/main/java/xxx/yyy/zzz/feature/settings/impl/
│   ├── SettingsModule.kt
│   ├── SettingsNavigation.kt
│   ├── SettingsViewModel.kt
│   └── SettingsScreen.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `object featureSettingsModule` | Koin 模块，注册 SettingsViewModel |
| `EntryProviderScope<NavKey>.settingsEntry(...)` | Navigation 3 路由注册扩展函数 |
| `enum class SettingsItem` | 设置项枚举：`UserAgreement` / `PrivacyPolicy` / `AboutApp` / `GoToHome` |

---

## 使用示例

```kotlin
// 在 Koin Application 模块中
modules(featureSettingsModule)

// 在 NavHost 中注册路由
NavHost(entries) {
    settingsEntry(
        onNavigate = { key -> navigator.navigate(key) },
        onBack = { navigator.goBack() }
    )
}
```

---

## 测试

- SettingsViewModel 当前无业务逻辑
- UI 测试可使用 Compose Testing 验证列表项的渲染和点击

---

> **最后更新：** 2026-06-25
