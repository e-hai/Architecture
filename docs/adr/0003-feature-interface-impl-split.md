# 3. Feature 模块 Interface 与 Implementation 隔离设计

- 状态: Accepted
- 日期: 2026-11-02
- 决策者: iOS 架构团队

## 背景与问题陈述

大型工程中业务模块之间存在交叉跳转与数据共享诉求。如果模块间直接相互依赖实现，极易形成循环依赖（Circular Dependency），导致构建严重变慢且无法解耦。

## 决策驱动因素

- 彻底避免业务模块之间的循环依赖
- 提升增量编译速度（修改 Impl 不需要重编依赖它的外部模块）
- 便于独立编写单元测试与模块级别 Fake 替换

## 决策结果

选定选项：每个业务功能拆分为两个 Target：
1. `Feature*Interface`：只包含路由 Key/Destination、对外 Protocol、共享参数模型；
2. `Feature*Impl`：包含完整的 UI、ViewModel、内部仓储实现。
业务间仅依赖 Interface，运行时由 `App` 顶层统一装配。
