# Module: :core:data

Repository 层。组合 Remote（network）和 Local（database + datastore）数据源，实现**离线优先**策略。

---

## 依赖

- `:core:model` — 领域模型
- `:core:database` — 本地 Room 数据库
- `:core:datastore` — 本地键值存储
- `:core:network` — 远程网络 API

---

## 目录结构

```
core/data/
├── src/main/java/xxx/yyy/zzz/core/data/
│   ├── UserRepository.kt
│   ├── UserRepositoryImpl.kt
│   └── DataModule.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `interface UserRepository` | 用户仓库接口：`getUserStream(): Flow<User?>`（观察）、`syncUser(): Result<Unit>`（同步） |
| `object coreDataModule` | Koin 模块，绑定 `UserRepository` 到 `UserRepositoryImpl` |

---

## 使用示例

```kotlin
// 在模块 build.gradle.kts 中
dependencies {
    implementation(project(":core:data"))
}

// 获取用户数据流（UI 层订阅）
val userStream: Flow<User?> = userRepository.getUserStream(userId)

// 触发网络同步（ViewModel 调用）
viewModelScope.launch {
    userRepository.syncUser(userId)
}
```

---

## 测试

Repository 的测试应手写 Fake `UserRepository` 实现，使用内存集合模拟数据源行为。

---

> **最后更新：** 2026-06-25
