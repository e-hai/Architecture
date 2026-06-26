# Module: :core:ui

公共 UI 基础设施。包含应用主题、颜色定义和可复用 Composable 组件。

---

## 依赖

- `myproject.android.compose` 约定插件（自动引入 Compose BOM、Material3、Icons Extended 等）

---

## 目录结构

```
core/ui/
├── src/main/java/xxx/yyy/zzz/core/ui/
│   ├── Color.kt
│   ├── Theme.kt
│   └── LoadingView.kt
└── build.gradle.kts
```

---

## 公共 API

| 类型 | 说明 |
|------|------|
| `MyProjectTheme(darkTheme, dynamicColor, content)` | Material3 主题，自动适配动态取色（Android 12+） |
| `LoadingView(modifier)` | 居中 CircularProgressIndicator |
| `Purple80`, `PurpleGrey80`, `Pink80` | 深色主题色值 |
| `Purple40`, `PurpleGrey40`, `Pink40` | 浅色主题色值 |

---

## 使用示例

```kotlin
// 在 Composable 中使用主题
MyProjectTheme {
    // 应用内所有 Composable
}

// 使用加载视图
LoadingView(modifier = Modifier.fillMaxSize())
```

---

## 测试

主题和静态组件无需测试。LoadingView 可添加 Composable 预览测试。

---

> **最后更新：** 2026-06-25
