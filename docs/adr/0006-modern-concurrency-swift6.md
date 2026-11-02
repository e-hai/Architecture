# 6. 全面采用 Swift 6 现代并发模型 (Structured Concurrency)

- 状态: Accepted
- 日期: 2026-11-02
- 决策者: iOS 架构团队

## 背景与问题陈述

传统基于 GCD / 回调闭包（Completion Handlers）的多线程开发极易引发数据竞争（Data Race）、死锁与线程爆炸问题。

## 决策驱动因素

- 编译期数据竞争安全检查
- 扁平化异步代码，显著提升代码可读性与异常处理能力
- 结构化任务生命周期管理（自动取消与级联传播）

## 决策结果

选定选项：
- 全面使用 `async`/`await`、`Task`、`Actor`；
- 所有跨模块跨线程传输的数据模型强制遵循 `Sendable`；
- UI 主线程刷新统一绑定 `@MainActor`。
