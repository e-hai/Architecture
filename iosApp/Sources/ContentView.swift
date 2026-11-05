import SwiftUI

#if canImport(Shared)
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
#endif

public struct ContentView: View {
    public init() {}

    public var body: some View {
        #if canImport(Shared)
        ComposeView()
            .ignoresSafeArea()
        #else
        VStack(spacing: 16) {
            Text("Architecture KMP - iOS Host")
                .font(.headline)
            Text("Open in Xcode to run with Shared.framework")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding()
        #endif
    }
}
