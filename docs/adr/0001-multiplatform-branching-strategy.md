# 1. 采用多分支隔离策略管理多平台移动端架构方案

- 状态: Superseded (已于 2026-11-24 收敛为单分支 KMP 模板仓，原多分支已归档至 `archive/*` Tag)
- 日期: 2026-11-02
- 决策者: 移动端架构委员会

## 背景与问题陈述

移动端存在不同的主流技术选型与平台路线：
1. **Android 原生**：深植于 Gradle 构建体系、Kotlin、Jetpack Compose 与 Now in Android 范式；
2. **iOS 原生**：基于 Apple Xcode、Swift 6 结构化并发、SwiftUI 与 SPM 本地模块化；
3. **跨平台方案 (KMP)**：基于 Kotlin Multiplatform 共享底层逻辑，跨端统一渲染。

如果将所有平台的完整工程强行堆砌在单一分支中，会导致构建工具链冲突（Gradle、SPM、CocoaPods 混杂）、体积膨胀、开发者依赖干扰严重。

## 决策驱动因素

- 保持各平台工程代码库的极致轻量与开箱即用体验
- 杜绝无关平台工具链环境对特定开发者的干扰
- 架构哲学与设计范式（三级模块化、Kit 门面、离线优先）跨平台统一规范

## 决策结果

选定选项：
- **`main` 分支**：作为全局架构门户与选型中枢，存放多端选型矩阵、全局规则、ADR 记录与各分支导航；
- **`android` 分支**：纯净的 Android 原生现代架构骨架；
- **`ios` 分支**：纯净的 iOS 原生现代架构骨架；
- **`kmp` 分支**：纯净的 Compose Multiplatform 跨端共享架构骨架。

### 积极影响

- 各平台开发者可通过 `git clone -b <platform>` 快速拉取对应干净的独立工程模板；
- 避免不同平台工具链（Gradle 与 SPM/Xcode）在同一工作区冲突；
- 主分支清晰呈现全局架构设计思考与技术矩阵。

## 演进说明 (2026-11-24)

随着脚手架确定以 Compose Multiplatform (KMP + CMP) 作为唯一跨平台模板主干，若继续保留多平台多分支结构，会导致通过 GitHub **Use this template** 创建新项目时复制出 `android`、`ios`、`main` 等多余分支，每次均需人工重置默认分支并删枝。

因此工程已收敛为**单分支（仅 `main`）KMP 模板仓**：
- 原 `kmp` 分支完整保留历史并提升为唯一默认 `main` 分支（`Use this template` 开箱即用、零分支清理成本）；
- 原分支历史已通过只读归档 Tag 永久保留：`archive/portal-main`、`archive/android`、`archive/ios`、`archive/feature-shortvideo`。
