# 0002. 采用 JetBrains 2026 最新 KMP 默认项目结构 (androidApp + shared + iosApp)

* 状态: Accepted
* 日期: 2026-11-04

## 背景与问题上下文

在旧版 Kotlin Multiplatform (KMP) 项目模板中，通常依赖单体 `composeApp` 模块。该模块将 Android 应用入口（`MainActivity`、`Application`、`AndroidManifest.xml`）、打包混淆签名、多平台编译（Kotlin Multiplatform Gradle 插件）与 Compose UI 混杂在同一个 Gradle 模块中。

随着 Android Gradle Plugin (AGP) 9.0+ 的演进，AGP 严格禁止在同一 Gradle 模块中同时应用 `com.android.application` 与 `org.jetbrains.kotlin.multiplatform` 库插件。此外，旧结构导致平台宿主边界模糊，不利于后续拓展多宿主端（如 Desktop、Web）。

## 决策依据与选项

1. **选项 1：旧版单体 `composeApp` 结构**
   - 优缺点：在小 Demo 中目录扁平，但在大型项目中职责不清，且与 AGP 9.0 存在严重破坏性冲突。
2. **选项 2：JetBrains 2026 最新官方标准（`androidApp` + `shared` + `iosApp`）**
   - 优缺点：职责清晰，`androidApp` 专职打包应用，`shared` 专职多平台 UI 与逻辑共享并导出 iOS Framework，100% 兼容 AGP 9.0。

## 最终决策

选择 **选项 2**。将工程拆解为：
- `androidApp`：独立 Android 宿主（应用 `com.android.application`）；
- `shared`：纯 KMP 共享 UI 与导航库（应用 `myproject.kmp.library` + Compose Multiplatform）；
- `iosApp`：独立 iOS Swift 宿主（Xcode / SPM）。

## 正反面影响

### 正面影响
* 100% 契合 JetBrains 2026 最新官方规范与 AGP 9.0 演进方向；
* 平台打包逻辑与跨端共享逻辑物理隔离，构建增量更快；
* 未来拓展新端（如 `desktopApp`、`webApp`）只需新增同级宿主模块消费 `shared` 即可。

### 负面影响
* 相比极简单体 Demo，多增加了一个 `androidApp` 顶层模块。
