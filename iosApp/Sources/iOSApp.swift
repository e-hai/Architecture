import SwiftUI

@main
public struct iOSApp: App {
    public init() {
        KmpKitBridgeBootstrapper.installAll()
    }

    public var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

public struct iOSAppView: View {
    public init() {
        KmpKitBridgeBootstrapper.installAll()
    }

    public var body: some View {
        ContentView()
    }
}
