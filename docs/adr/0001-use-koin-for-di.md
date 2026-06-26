# ADR-001: 使用 Koin 作为依赖注入框架

## 状态

Accepted

---

## 背景

项目需要依赖注入方案来管理模块间的依赖关系。Android 生态中主流的 DI 方案有 Hilt、Dagger 和 Koin。项目作为脚手架需要兼顾编译速度、学习门槛和代码可读性。

## 备选方案

- **Hilt**（基于 Dagger + 注解处理器）：官方推荐，支持 `@HiltViewModel`，但需要 KSP/KAPT 注解处理步骤，增加编译时间，`@Inject` 注解侵入性强。
- **Dagger**：编译时生成代码，但 API 复杂，学习曲线陡峭，模板代码多。
- **Koin**：Kotlin 编写的运行时 DI 框架，使用 DSL 声明依赖，无注解处理器。

## 决策

使用 **Koin 4.2.1** 作为依赖注入框架。通过约定插件 `myproject.koin` 统一配置，自动引入 koin-core、koin-android、koin-androidx-compose 三个依赖。

在 `build-logic/convention/` 中封装为可组合的约定插件，各模块按需应用：
```kotlin
plugins {
    alias(libs.plugins.myproject.koin)
}
```

## 理由

- 无注解处理器，编译速度快于 Hilt/Dagger
- DSL 声明式 API，代码简洁易读
- 与 Kotlin 语言特性自然融合（lambda、扩展函数）
- 无需 `@Inject` 注解侵入业务代码
- 约定插件封装后，模块只需一行声明即可启用 DI

## 影响

### 正面影响

- 编译速度比 Hilt/Dagger 快（跳过注解处理步骤）
- 学习成本低，新成员可快速上手
- DI 配置集中在 Koin 模块中，不散落在类上
- 与 Jetpack Compose 集成良好（`koinViewModel()`）

### 负面影响

- 依赖关系在运行时解析，错误在运行时才暴露（如循环依赖、缺少绑定）
- 不支持编译时依赖图校验
- 无法通过编译时的可达性分析找未使用的绑定

### 中立影响

- 通过接口 + 实现分离来配合 DI（`single<Interface> { Impl() } bind Interface::class`）

---

## 遵循情况

- 禁止使用 Hilt、Dagger，禁止 `@Inject`、`@HiltViewModel` 注解
- 所有 ViewModel 通过 `viewModel { }` 在 Koin 模块中声明
- CoroutineDispatcher 必须通过 Koin 注入，禁止硬编码 `Dispatchers.IO`
