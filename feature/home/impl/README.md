# Module: :feature:home:impl

Home 功能模块的实现模块。包含主页、详情页、结果页三个页面，以及对应的 ViewModel、导航注册和 Koin 模块。

---

## 依赖

- `:feature:home:api` — 导航键定义
- `:core:model` — 领域模型
- `:core:data` — 数据仓库

---

## 目录结构

```
feature/home/impl/
├── src/main/java/xxx/yyy/zzz/feature/home/impl/
│   ├── HomeModule.kt
│   ├── HomeNavigation.kt
│   ├── HomeViewModel.kt
│   ├── HomeScreen.kt
│   ├── HomeDetailViewModel.kt
│   ├── HomeDetailScreen.kt
│   ├── HomeResultViewModel.kt
│   └── HomeResultScreen.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `object featureHomeModule` | Koin 模块，注册 HomeViewModel、HomeDetailViewModel、HomeResultViewModel |
| `EntryProviderScope<NavKey>.homeEntry(...)` | Navigation 3 路由注册扩展函数 |

### UiState

| 类型 | 说明 |
|------|------|
| `HomeUiState` | 密封接口：`Loading` / `Success(featuredItems, recentItems)` / `Error(message)` |
| `HomeDetailUiState(title: String)` | 详情页 UI 状态 |
| `HomeResultUiState(originalTitle: String, editedTitle: String)` | 结果页（编辑器）UI 状态 |

---

## 使用示例

```kotlin
// 在 Koin Application 模块中
modules(featureHomeModule)

// 在 NavHost 中注册路由
NavHost(entries) {
    homeEntry(
        onNavigate = { key -> navigator.navigate(key) },
        onBack = { navigator.goBack() }
    )
}
```

---

## 测试

- ViewModel 测试使用 Turbine 验证 `uiState` Flow
- 使用 Fake `UserRepository` 替代真实实现
- Composable 测试使用 Compose Testing + 语义操作

---

> **最后更新：** 2026-06-25
