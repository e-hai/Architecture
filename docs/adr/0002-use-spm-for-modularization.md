# 2. 使用 Swift Package Manager (SPM) 进行工程模块化拆分

- 状态: Accepted
- 日期: 2026-11-02
- 决策者: iOS 架构团队

## 背景与问题陈述

随着项目功能迭代，单体单工程会导致编译瓶颈、职责模糊与强耦合。iOS 传统模块化方案依赖 CocoaPods 或庞大的 `.xcodeproj` 嵌套工程，维护成本与 Git 冲突概率高。

## 决策驱动因素

- 官方原生支持，无第三方工具链（如 Ruby / Gem / Bundler）依赖
- 模块边界清晰，显式声明依赖关系
- 命令行友好，支持 `swift build` 与 `swift test` 快速验证

## 决策结果

选定选项：采用 **SPM 本地包（Local Swift Packages）** 构建多模块体系：
- 核心基础设施收敛在 `Packages/Core`，通过多个 Target 导出独立能力；
- 业务 Feature 模块采用独立 SPM Target，隔离 Interface 与 Implementation。
