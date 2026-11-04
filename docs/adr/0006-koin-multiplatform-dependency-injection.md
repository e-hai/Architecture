# 0006. Koin 跨平台依赖注入与 AppBootstrap 装配范式

* 状态: Accepted
* 日期: 2026-11-04

## 背景与问题上下文

在多平台架构中，需要统一的依赖注入 (DI) 容器来解耦业务特性与基础设施，并便于在测试中替换实现。

## 决策依据与选项

1. **选项 1：纯手工构造函数注入**
   - 优缺点：零第三方依赖，但随着模块增多，顶层装配工厂代码冗长繁琐。
2. **选项 2：Kotlin 编译期注解注入 (kotlin-inject)**
   - 优缺点：编译期检查强，但与 Android 分支的 Koin 语法习惯不同。
3. **选项 3：Koin 4.x Multiplatform**
   - 优缺点：与 Android 分支保持 100% 对齐的 DSL 体验，支持 `koin-compose` 与 `koin-compose-viewmodel`。

## 最终决策

选择 **选项 3**。通过 `AppBootstrap.doInitKoin` 统一注册，各端在启动时传入平台特定配置（如 Android `androidContext`）。
