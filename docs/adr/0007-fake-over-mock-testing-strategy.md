# 0007. 手写强类型 Fake 替代 Mock 跨端测试策略

* 状态: Accepted
* 日期: 2026-11-04

## 背景与问题上下文

传统的动态运行时 Mock 框架（如 Mockk / Mockito）在 Kotlin/Native（iOS 编译目标）中兼容性极差且运行时开销大。

## 决策依据与选项

1. **选项 1：引入动态 Mock 框架**
   - 优缺点：在 Native 端经常发生字节码改写失效或空指针闪退，破坏多平台测试一致性。
2. **选项 2：手写强类型 Fake 实现 (Fake over Mock)**
   - 优缺点：行为可预测、编译期类型安全，100% 兼容 Kotlin/JVM 与 Kotlin/Native，测试执行速度极快。

## 最终决策

选择 **选项 2**。禁止在项目中引入动态运行时 Mock 框架，所有基础设施与外部数据源（如 `FakeNetworkDataSource`、`FakeKeyValueStorage`）均提供手写强类型 Fake。
