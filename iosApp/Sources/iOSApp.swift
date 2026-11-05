import SwiftUI

#if canImport(Shared)
import Shared

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
