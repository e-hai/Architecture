# ADR-010: 使用 Jetpack Compose 构建 UI

## 状态

Accepted

---

## 背景

项目需要选择 Android UI 框架。传统方案是 XML + View system，但 Google 已明确声明 Compose 为 Android 推荐的 UI 开发方式，并停止为 View system 提供新的特性更新。同时项目采用了 Kotlin 2.0+ 和模块化架构，需要一个与 Kotlin 语言特性和模块化体系高度契合的 UI 方案。

## 备选方案

### 方案 A：XML + View System（传统方案）

使用 XML 布局文件 + Activity/Fragment + ViewBinding。

**优点：**
- 技术成熟，社区资源丰富
- 兼容所有 Android 版本
- 无需额外学习成本

**缺点：**
- 布局与逻辑分离，开发效率低
- 状态管理繁琐（需要手动刷新 UI）
- 与 Kotlin 语言特性（协程、Flow）配合不够自然
- Fragment 生命周期复杂
- Google 已停止为 View system 提供 Compose 级别的特性更新

### 方案 B (选定)：Jetpack Compose

完全使用 Compose 声明式 UI，配合 Kotlin Compiler Plugin（Kotlin 2.0+ 内建）。

**优点：**
- 声明式 UI，状态驱动，状态变更自动重组
- 与 Kotlin 协程、Flow、ViewModel 原生配合
- 天然模块化，Composable 函数可作为独立单元
- 减少模板代码（无 XML、无 Fragment、无 Adapter）
- 与 navigation3、lifecycle-viewmodel-compose 等 Jetpack 库无缝整合
- Kotlin 2.0+ 的 Compose Compiler Plugin 无需独立配置版本号

**缺点：**
- 学习曲线（重组机制、副作用、状态提升等概念）
- 调试需要熟悉 Compose Inspector 等工具
- 自定义绘制/Accessibility 在某些场景下不如 View system 成熟

### 方案 C：Flutter（跨平台）

**缺点：**
- 偏离 Android 原生技术栈，与项目其他选型（Koin、Room、Navigation3）不兼容
- 需引入 Dart 语言和 Flutter SDK，团队学习成本高
- 不适合 Android-only 项目

## 决策

UI **完全使用 Jetpack Compose**，**禁止 XML 布局和 ViewBinding**。

### 版本配置

- **Compose BOM**：`2026.05.01`（统一管理 UI 库版本）
- **Compose Compiler**：Kotlin 2.0+ 内建（`org.jetbrains.kotlin.plugin.compose`），无需指定 compiler 版本
- **Material Design 3**：通过 BOM 引入 `material3`

### 依赖体系

通过 `myproject.android.compose` 约定插件统一引入：

| 依赖 | 作用 | 配置 |
|------|------|------|
| `compose-bom` (BOM) | 统一管理 Compose 版本 | implementation + androidTestImplementation |
| `compose-ui` | 核心 UI（绘制、输入、手势） | implementation |
| `compose-ui-graphics` | Canvas、Shape、Color 等图形 API | implementation |
| `compose-ui-tooling-preview` | `@Preview` 注解支持 | implementation |
| `compose-material3` | Material Design 3 组件库 | implementation |
| `material-icons-extended` | 扩展图标库 | implementation |
| `foundation` | 基础组件（LazyColumn, Pager 等） | implementation |
| `lifecycle-runtime-compose` | Lifecycle 感知的 Compose 支持 | implementation |
| `lifecycle-viewmodel-compose` | ViewModel 在 Compose 中的获取 | implementation |
| `compose-ui-tooling` | Layout Inspector 等调试工具 | debugImplementation |
| `compose-ui-test-manifest` | Compose 测试 Manifest 配置 | debugImplementation |

## 理由

- 与 Kotlin 2.0+ 和 Navigation3 完美整合，编译器内置 Compose 支持
- 声明式 UI 与 ViewModel 的 `StateFlow` 组合天然适合响应式编程
- 模块化友好：Composable 函数可作为独立组件在各模块之间复用
- 无 XML 意味着资源合并减少，模块边界更清晰
- Material Design 3 + Icons Extended 覆盖绝大多数 UI 需求

## 影响

### 正面影响

- 开发效率提升（状态驱动 UI，无需手动刷新）
- 代码量减少（无 XML、无 Adapter、无 Fragment）
- 类型安全：路由、状态、UI 全部 Kotlin 编译期检查
- 统一技术栈，团队只需掌握 Compose 一种 UI 技术

### 负面影响

- 对不熟悉 Compose 的开发者需要学习曲线
- 与传统第三方 Android UI 库的兼容性可能受限
- 自定义复杂的绘制/动画需要深入理解 Compose 内部机制

### 中立影响

- 所有 feature 模块自动通过 `myproject.android.compose` 插件引入 Compose 依赖
- 基础库（`core:ui`）负责主题、设计系统组件
- Feature 模块（`feature:*:impl`）负责自己的 Screen Composable

---

## 遵循情况

- 项目中**禁止**出现 `.xml` 布局文件（除 `strings.xml` 和 `themes.xml` 等资源文件外）
- 所有 feature 模块必须依赖 `myproject.android.compose`（通过 `myproject.android.feature.impl` 间接引入）
- Composable 必须无状态：不持有 ViewModel，状态通过参数向下传递
- UI 测试使用 Compose Testing API，不使用 Espresso
