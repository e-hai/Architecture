# ADR-005: Feature 模块采用 api/impl 分离策略

## 状态

Accepted

---

## 背景

随着模块数量增加，模块间的依赖关系需要严格管理。直接依赖 feature 模块的 impl 实现会导致实现细节泄露、编译时隔离不足、循环依赖风险增加。参考 Now in Android 的分层设计，决定对每个 feature 模块进行 api/impl 拆分。

## 备选方案

- **单体 Feature 模块**：一个模块包含接口和实现，外部直接依赖。实现细节对外可见，重构时影响范围大。
- **api/impl 分离**：拆分为两个模块，api 只暴露公共接口（NavKey、数据类、接口定义），impl 包含具体实现（UI、ViewModel）。外部只依赖 api 模块。

## 决策

每个 feature 拆分为两个 Gradle 模块：
- `:feature:xxx:api` — 只包含 `@Serializable NavKey`、共享数据类、接口定义。**无 UI、无实现代码。**
- `:feature:xxx:impl` — 包含 Screen Composable、ViewModel、Koin 模块、资源文件。

依赖关系：`app → feature:impl → feature:api`。外部模块只依赖 `:feature:xxx:api`。

`feature:impl` 通过 `myproject.android.feature.impl` 约定插件自动配置 Compose + Koin，并自动依赖 `:core:ui` 和 `:core:navigation`。

## 理由

- 编译时强制隔离：impl 模块的修改不会触发依赖 api 的模块重新编译
- 降低重构成本：只要 api 接口不变，impl 完全重写不影响外部调用者
- 依赖方向清晰：数据流单向（app → impl → api → core），避免循环依赖
- 导航隔离：跨模块导航只需要 api 模块的 NavKey 类，无需引入整个 impl

## 影响

### 正面影响

- 模块边界清晰，职责单一
- 编译隔离，增量编译效率更高
- 实现模块可独立替换或废弃
- 降低大型团队并行开发的冲突概率

### 负面影响

- 模块数量翻倍（每个 feature 从 1 个变为 2 个）
- 新增 feature 需要创建两个模块和对应的 build.gradle.kts
- 跨模块导航需要经过 api 模块间接暴露 NavKey，多一层导入路径

### 中立影响

- api 模块应用 `myproject.android.feature.api` 插件（自动引入 Library + Serialization + Navigation3）
- impl 模块应用 `myproject.android.feature.impl` 插件（自动引入 Library + Compose + Koin）

---

## 遵循情况

- 所有 feature 模块必须拆分为 api/impl 两个子模块
- api 模块禁止包含 Compose 依赖和 UI 代码
- 外部模块只依赖 api 模块，不直接依赖 impl
- 新增 feature 时遵循相同的 api/impl 模式
