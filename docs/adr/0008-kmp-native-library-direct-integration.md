# 0008. KMP 原生跨平台库直连架构 (以 PagKit 为例)

* 状态: Accepted
* 日期: 2026-11-11

## 背景与问题上下文

在接入 Tencent libpag 动效引擎时，存在自研已构建并封装好的跨平台组件库 `io.github.e-hai:pagkit`。
在前期 ADR-0004 中，商业化原生 Kit 均采用「100% 纯 `commonMain` 门面 + 宿主私有 Delegate 桥接」的范式 A。
面对本身就是纯 Kotlin Multiplatform + Compose Multiplatform 跨平台构建并包含双端二进制（JVM 字节码与 iOS klib）的库，需要裁定是否继续强制沿用 Delegate 桥接模式，还是采用直接依赖封装的范式 B。

## 备选方案

1. **方案 1：强制套用 Delegate 模式 (范式 A)**
   - 在 `core:pag` 中定义 `CorePag` 门面与 `PagDelegate` 接口，在 `androidApp` 中引用 Android libpag SDK，在 `iosApp` 中通过 Swift/Obj-C 桥接 iOS libpag SDK。
   - **缺点**：严重破坏已有的 `PagKit` 跨平台 Compose 组件封装，迫使双端宿主各自手写复杂的平台视图适配器（Android `ComposeView` 与 iOS `UIKitView`），产生大量无意义的胶水代码与状态同步开销。
2. **方案 2：KMP 原生库直连门面包装 (范式 B)**
   - `:core:pag` 直接在 `commonMain` 中 `implementation(libs.pagkit)`；
   - 对外暴露 `CorePag` 统一单例门面，并 re-export 导出 `PagView`、`rememberPagState`、`PagSource` 等 CMP 组件；
   - 宿主仅需处理平台底层运行时初始化（如 Android 的 Native so 库解压 `PagKitAndroid.init(context)`，iOS 工程链接动态 framework）。

## 最终决策

选择 **方案 2 (范式 B)**。
将 Kit 体系清晰划分为两大模式：
- **范式 A（原生委托透传）**：适用于双端独立的原生 SDK（AAR / Swift SPM），保持 `commonMain` 绝对轻量与宿主 Delegate 隔离；
- **范式 B（KMP 原生直连）**：适用于已实现 KMP/CMP 标准化的跨平台 SDK，在 `core:*` 模块直接引入并统一暴露门面与 UI 控件。

## 影响

### 正面影响
* 极大简化跨平台动效控件的调用，上层业务与 `shared` 模块直接使用标准的声明式 Compose 代码（`PagView(...)`）；
* 避免了在宿主层二次实现 Native View 封装与状态机同步；
* 架构规范扩展为双接入范式，为后续更多 CMP/KMP 原生第三方库接入树立标准化准则。

### 负面影响与注意事项
* `:core:pag` 具备真实的跨端实现依赖，而非仅有接口；因此需确保该 KMP 依赖稳定、测试健全。
