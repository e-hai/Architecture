# Module: :core:navigation

导航基础设施。封装 Navigation 3 Runtime，提供多级 backstack 管理和统一的导航操作接口。

---

## 依赖

- `libs.androidx-navigation3-runtime` — Navigation 3 运行时
- `libs.androidx-navigation3-ui` — Navigation 3 UI 集成
- `libs.androidx-lifecycle-viewModel-navigation3` — ViewModel 与导航生命周期绑定

---

## 目录结构

```
core/navigation/
├── src/main/java/xxx/yyy/zzz/core/navigation/
│   ├── Navigator.kt
│   └── NavigationState.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `class Navigator(state: NavigationState)` | 导航操作入口：`navigate(key)`、`goBack()` |
| `class NavigationState` | 状态容器，管理顶层 Tab backstack + 每个 Tab 的子 backstack |
| `rememberNavigationState(startKey, topLevelKeys)` | Composable 函数，创建 NavigationState 实例 |
| `NavigationState.toEntries(entryProvider)` | 将多栈状态转换为可渲染的 `NavEntry` 列表 |

---

## 使用示例

```kotlin
// 在 Activity 中设置导航
val state = rememberNavigationState(
    startKey = HomeNavKey,
    topLevelKeys = setOf(HomeNavKey, SettingsNavKey)
)
val navigator = Navigator(state)

// 页面导航
navigator.navigate(HomeDetailNavKey(id = 1, title = "Item"))

// 返回上一页
navigator.goBack()
```

---

## 测试

NavigationState 的单元测试可验证：Tab 切换逻辑、返回栈管理、重复选择 Tab 的栈清零策略。无需 Koin 注入。

---

> **最后更新：** 2026-06-25
