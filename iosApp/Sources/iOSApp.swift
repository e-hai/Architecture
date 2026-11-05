import SwiftUI

#if canImport(Shared)
import Shared

@main
public struct iOSApp: App {
    public init() {
        KmpKitBridgeBootstrapper.installAll()
        MainViewControllerKt.doInitKoinIos()
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
        MainViewControllerKt.doInitKoinIos()
        #endif
    }

    public var body: some View {
        ContentView()
    }
}
