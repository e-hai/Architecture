# 0003. 采用 Compose Multiplatform 实现跨端统一 UI

* 状态: Accepted
* 日期: 2026-11-04

## 背景与问题上下文

在多平台跨端开发中，UI 层有「双端原生原生 UI（SwiftUI + Jetpack Compose）」与「声明式统一 UI（Compose Multiplatform）」两种路线。
为了在 `kmp` 分支最大化跨端复用率并降低多端维护成本，需要明确跨端 UI 技术选型。

## 决策依据与选项

1. **选项 1：纯逻辑共享 + 原生 UI（KMP 仅共享 Kotlin ViewModel / Repository，iOS 编写 SwiftUI）**
   - 优缺点：双端 UI 完全独立，需写两套页面布局代码。
2. **选项 2：Compose Multiplatform (CMP) 统一 UI**
   - 优缺点：95%+ UI 代码集中在 `commonMain`，一套 Material3 主题与状态驱动逻辑，双端像素级一致。

## 最终决策

选择 **选项 2**。
在 `shared` 模块中通过 `App.kt`、`AppNavGraph` 与 `MyProjectTheme` 承载 CMP 根视图，通过 `MainViewController` 导出为 `UIViewController` 嵌入 `iosApp`。

## 正反面影响

### 正面影响
* 界面业务逻辑与 UI 布局 100% 共享，单人全栈交付多端特性；
* 支持系统深色模式自适应切换与 Compose 声明式状态流。
