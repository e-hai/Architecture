# Module: :core:model

共享领域模型。定义在模块间传递的纯数据类，不包含任何业务逻辑。

---

## 依赖

- `libs.kotlinx-serialization-json` — User 序列化支持

---

## 目录结构

```
core/model/
├── src/main/java/xxx/yyy/zzz/core/model/
│   ├── ListItem.kt
│   └── User.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `data class ListItem(id: Int, title: String)` | 列表项数据，无序列化注解 |
| `@Serializable data class User(id: String, name: String, email: String, avatarUrl: String)` | 用户数据，可跨模块序列化传递 |

---

## 使用示例

```kotlin
// 在模块 build.gradle.kts 中
dependencies {
    implementation(project(":core:model"))
}
```

模型类直接导入使用，无需 Koin 注入。

---

## 测试

纯数据类，无需测试。

---

> **最后更新：** 2026-06-25
