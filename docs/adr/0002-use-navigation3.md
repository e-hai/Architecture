# ADR-002: 使用 Navigation 3 实现类型安全导航

## 状态

Accepted

---

## 背景

项目需要导航解决方案以支持多页面跳转和自适布局（列表 - 详情）。Navigation Compose（Navigation 2）已稳定但其路由基于字符串，类型不安全。项目采用 `@Serializable` 路由定义，需要原生支持类型安全导航和多 backstack 的库。

## 备选方案

- **Navigation Compose（Navigation 2）**：稳定、文档丰富，但路由为字符串键，不支持编译时校验，多 backstack 支持有限（需手动管理）。
- **Navigation 3**：实验性库，支持 `@Serializable` 类型安全路由，原生多 backstack 管理。生命周期集成良好（`lifecycle-viewmodel-navigation3`）。
- **Voyager**：第三方导航库，功能完整但不属于 Jetpack 生态，长期维护风险不确定。
- **Decompose**：Badoo 开发的组件化导航方案，功能强大但学习曲线陡峭。

## 决策

使用 **Navigation 3 Runtime + UI 1.2.0-alpha03** 作为导航框架，配合 `lifecycle-viewmodel-navigation3`。

核心设计：
- 路由定义为 `@Serializable data class/object` 实现 `NavKey` 接口，禁止字符串路由
- 在 `core:navigation` 模块中封装 `Navigator` 和 `NavigationState`，提供统一的导航操作接口
- 自定义 `NavigationState` 管理两级 backstack（顶部 tab 切换 + 每个 tab 的子导航栈）

## 理由

- `@Serializable` 路由在编译时校验，避免字符串路由的拼写错误和参数类型不匹配
- 原生多 backstack 支持，完美适配顶部 Tab + 子页面嵌套的导航模式
- 与 Jetpack 生态深度集成，ViewModel 作用域与导航声明周期自动绑定
- Navigation 3 是 Jetpack 官方发展方向，Navigation 2 的长期替代方案

## 影响

### 正面影响

- 类型安全导航，编译时校验路由和参数
- 内置多 backstack 管理，无需手动维护回退栈
- ViewModel 自动绑定到 NavBackStackEntry，无需额外作用域管理
- 支持自适应布局（ListDetailSceneStrategy）

### 负面影响

- 库仍为 alpha 版本，API 可能发生不兼容变更
- 社区资源少，遇到问题难以快速找到答案
- 需要额外学习 Navigation 3 的概念（NavBackStack、NavEntryDecorator 等）
- `core:navigation` 的 Navigator 封装增加了间接层

### 中立影响

- 跨模块导航通过 `:feature:xxx:api` 模块暴露的 NavKey 进行

---

## 遵循情况

- 所有路由必须使用 `@Serializable data class/object`，禁止字符串路由
- 禁止在 ViewModel 中直接导入 NavController
- 所有导航操作通过 `core:navigation` 的 Navigator 封装进行
- 跨模块导航只依赖 feature api 模块的 NavKey
