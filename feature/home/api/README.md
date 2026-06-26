# Module: :feature:home:api

Home 功能模块的公共 API 模块。只暴露导航键和共享数据类，**不含任何 UI 或业务实现**。

---

## 依赖

- 无模块间依赖（`myproject.android.feature.api` 约定插件自动引入 Navigation3 + Serialization）

---

## 目录结构

```
feature/home/api/
├── src/main/java/xxx/yyy/zzz/feature/home/api/
│   └── HomeNavKey.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `@Serializable data object HomeNavKey` | 主页导航键 |
| `@Serializable data class HomeDetailNavKey(id: Int, title: String)` | 详情页导航键（携带 id + title） |
| `@Serializable data class HomeResultNavKey(id: Int, title: String, od: Int = 0)` | 结果页导航键（携带 id + title + 可选 od） |
| `@Serializable data class TitleEditResult(id: Int, title: String)` | 编辑结果 DTO，通过 ResultEventBus 传递 |

---

## 使用示例

```kotlin
// 从其他模块导航到 Home 页面
navigator.navigate(HomeNavKey)

// 携带参数导航到详情页
navigator.navigate(HomeDetailNavKey(id = 1, title = "Example"))
```

---

## 测试

纯数据类和序列化对象，无需测试。

---

> **最后更新：** 2026-06-25
