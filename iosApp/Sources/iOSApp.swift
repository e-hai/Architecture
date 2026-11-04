import SwiftUI

/**
 * iOS 原生 SwiftUI 宿主入口。
 */
public struct iOSAppView: View {
    public init() {
        KmpKitBridgeBootstrapper.installAll()
    }

    public var body: some View {
        VStack(spacing: 16) {
            Text("Architecture KMP - iOS Host")
                .font(.headline)
            Text("Ready to embed Shared.MainViewController()")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
}
