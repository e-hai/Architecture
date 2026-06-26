# Module: :module:path

（一行总结：该模块的职责是什么，属于哪一层）

---

## 依赖

- `:core:xxx` — （为什么需要这个依赖）
- `libs.xxx` — （第三方库用途）

---

## 目录结构

```
path/to/module/
├── src/main/java/xxx/yyy/zzz/...
│   ├── Xxx.kt
│   └── XxxModule.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `SomeClass` | 是什么，怎么用 |
| `xxxModule` | Koin 模块声明 |

---

## 使用示例

```kotlin
// 模块的 build.gradle.kts
dependencies {
    implementation(project(":module:path"))
}
```

---

## 测试

（测试方法，是否提供 Fake，使用的测试框架等）

---

> **最后更新：** YYYY-MM-DD
