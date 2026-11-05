import SwiftUI

#if canImport(Shared)
import Shared

/**
 * iOS 独立应用程序系统入口（遵循 SwiftUI App 协议）。
 *
 * 职责：
 * 1. 作为完整 iOS 可执行 App（.app）的独立主入口（标记 @main）；
 * 2. 负责顶层 WindowGroup 窗口生命周期与渲染容器配置；
 * 3. 执行冷启动标准初始化（Phase 1: SPM 原生 Kit Bridge 安装；Phase 2 & 3: 跨平台 Koin DI 容器与日志确认）。
 *
 * 注：仅在存在 Shared.framework 的完整应用工程中编译生效。
 */
@main
public struct iOSApp: App {
    public init() {
        // Phase 1: 安装 iOS 8 大原生 Kit SPM Bridge
        KmpKitBridgeBootstrapper.installAll()
        // Phase 2 & 3: 统一初始化跨平台容器与就绪日志
        AppBootstrap.shared.doInitCommon()
    }

    public var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
#endif

/**
 * 可复用根视图组件（遵循 SwiftUI View 协议）。
 *
 * 设计定位与应用场景：
 * 1. 单元测试沙盒（Unit & UI Testing）：
 *    在 Swift Testing / XCTest 单元测试环境下，避免 @main 导致的 duplicate symbol _main 符号冲突，
 *    测试用例可直接实例化本 View 进行视图初始化验证与快照测试。
 * 2. 混编嵌入模式（Add-to-App / Hybrid Integration）：
 *    当本项目作为 SPM 库（iosAppLib）被现有的大型外部宿主 App（已有自身的 @main AppDelegate/App）引入时，
 *    外部宿主无法也不应再声明第二个 @main，而是将 iOSAppView 作为普通 SwiftUI View 嵌入任意 TabBar 或页面中。
 * 3. Xcode 画布预览（#Preview）：
 *    支持在 Xcode Canvas 中直接通过 #Preview { iOSAppView() } 预览根视图渲染效果。
 */
public struct iOSAppView: View {
    public init() {
        KmpKitBridgeBootstrapper.installAll()
        #if canImport(Shared)
        AppBootstrap.shared.doInitCommon()
        #endif
    }

    public var body: some View {
        ContentView()
    }
}
