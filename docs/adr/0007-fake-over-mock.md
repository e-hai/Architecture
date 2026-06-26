# ADR-007: 测试使用手写 Fake 替代 Mock 框架

## 状态

Accepted

---

## 背景

单元测试需要隔离被测代码的外部依赖。Mock 框架（Mockito/MockK）通过运行时代理创建 Mock 对象，行为定义分散在各个测试方法中。重构接口时，Mock 行为定义不会报错（编译通过、运行失败），导致测试脆弱。

## 备选方案

- **Mockito/MockK**：运行时代理生成 Mock 对象，使用 when/thenReturn 定义行为。重构后行为定义可能静默失效，测试在运行时失败而非编译时。
- **手写 Fake**：为每个接口编写一个 Fake 实现类（内存中的真实实现），提供可控的测试替身。重构接口时 Fake 类编译会立即报错。
- **混合使用**：部分接口用 Fake，复杂场景用 Mock。但容易导致不统一，团队认知成本高。

## 决策

禁止使用 Mockito/MockK。必须为每个接口提供手写 Fake 实现。

Fake 实现要求：
- 对 Repository 接口的 Fake：使用内存 MutableList/MutableMap 存储数据，行为与真实实现一致但无副作用
- 对 DataSource 接口的 Fake：同理，使用内存集合
- ViewModel 测试：使用 Fake 构造 ViewModel，使用 Turbine 验证 StateFlow/Channel

## 理由

- 编译时验证：接口变更时 Fake 类编译报错，第一时间发现问题
- 行为集中定义：Fake 的真实行为集中在实现类中，而不是散落在每个测试的 when/thenReturn 中
- 测试可复用：同一份 Fake 可在多个测试间共享，减少重复
- 重构友好：重命名接口方法时，IDE 一键重构可同时更新 Fake

## 影响

### 正面影响

- 编译时反馈，接口变更立即发现
- 测试可读性提升，行为逻辑集中在 Fake 类
- 测试维护成本降低

### 负面影响

- 每个接口需要单独编写 Fake 实现类
- 简单接口的 Fake 编写可能比 Mock 多几行代码
- Fake 实现需要随接口一同维护更新

### 中立影响

- ViewModel 测试使用 Turbine 验证 Flow

---

## 遵循情况

- 项目依赖中不包含 mockito 或 mockk 库
- 每个公开接口必须在 `core/testing` 或其他测试源码集中的 `fake/` 包下提供 Fake 实现
- ViewModel 测试必须使用 Turbine + Fake
- UI 测试使用 Compose Testing + 语义操作
