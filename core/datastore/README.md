# Module: :core:datastore

DataStore Preferences 封装，提供类型安全的键值存储 API。

---

## 依赖

- `libs.datastore-preferences` — DataStore Preferences

---

## 目录结构

```
core/datastore/
├── src/main/java/xxx/yyy/zzz/core/datastore/
│   ├── UserPreferencesDataSource.kt
│   └── DatastoreModule.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `class UserPreferencesDataSource` | 封装 DataStore 操作，对外暴露 Flow（读）和 suspend（写） |
| `object coreDatastoreModule` | Koin 模块，提供 DataStore 实例和 DataSource |

---

## 使用示例

```kotlin
// 在模块 build.gradle.kts 中
dependencies {
    implementation(project(":core:datastore"))
}

// 读取（Flow 响应式订阅）
val lastSyncedUserId: Flow<String?> = dataSource.lastSyncedUserId

// 写入（suspend 异步）
dataSource.setLastSyncedUserId("user123")
```

---

## 测试

为 `UserPreferencesDataSource` 编写 Fake 实现，使用内存 MutableMap 替代 DataStore。

---

> **最后更新：** 2026-06-25
