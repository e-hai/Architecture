# 0004. 八大商业化 Kit 1:1 全原生透传与委托架构

* 状态: Accepted
* 日期: 2026-11-04

## 背景与问题上下文

`e-hai` 旗下的 8 大商业化与基础设施库目前在 GitHub 上是双端独立原生仓库：
- Android 端为 JitPack AAR 仓库；
- iOS 端为纯 Swift 6 SPM 仓库。

KMP 的业务代码在 `commonMain` 中编写，无法直接 `import` Android AAR 或 Swift Package。需要一种轻量、透明且兼顾跨端调用的接入方案。

## 决策依据与选项

1. **选项 1：在 KMP commonMain 中重写一套路由器分发引擎**
   - 优缺点：增加冗余逻辑，且与底层的双端 SDK 存在双重路由分发，维护成本高。
2. **选项 2：1:1 全原生透传（静态门面 + 内部 Delegate 接口）**
   - 优缺点：`commonMain` 暴露与 `android`/`ios` 分支一致的静态门面与业务常量；`androidMain` 1 行代码直连 JitPack 原生库；`iosApp` 在 Swift 中 1 行代码转发给 SPM 库。天然支持单元测试 Fake 注入。

## 最终决策

选择 **选项 2**。
在 `core:<kit>` 中统一提供 `*Delegate`、`*Keys / *Events / *Products / *Tags` 与静态门面对象。

## 正反面影响

### 正面影响
* 保持与原生 `android` / `ios` 分支调用体验 100% 对齐；
* 单行转发零额外开销；
* 单元测试可直接传入 Fake 实现，无需 Mock 框架。
