# 1. 使用 SwiftUI 与 Observation 框架作为核心 UI 与状态方案

- 状态: Accepted
- 日期: 2026-11-02
- 决策者: iOS 架构团队

## 背景与问题陈述

在设计现代 iOS 架构时，需要选择基础 UI 构建范式与状态驱动方案。iOS 存在传统的 UIKit 命令式视图与现代 SwiftUI 声明式视图，同时状态管理经历了 Combine `ObservableObject` 到 iOS 17+ 原生 Observation (`@Observable`) 的演进。

## 决策驱动因素

- 声明式 UI 范式与声明式单向数据流（UDF）的高度统一
- 与 Android Compose 现代声明式体系的设计心智对齐
- 最小化样板代码，提升页面渲染与刷新性能

## 决策结果

选定选项：全面采用 **SwiftUI**，状态层优先基于 **Observation 框架 (`@Observable`)**（并向下兼容 `ObservableObject`）。

### 积极影响

- 代码精简，消除大量 UIViewController 样板代码
- 基于属性级别的细粒度自动追踪刷新，避免不必要的全屏重绘
- 更好的跨 Apple 平台（iOS / iPadOS / macOS）适配能力
