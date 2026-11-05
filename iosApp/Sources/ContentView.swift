import SwiftUI

#if canImport(Shared)
import Shared

/**
 * Compose Multiplatform 视图桥接封装器。
 * 遵循 UIViewControllerRepresentable 协议，将 Kotlin/Native 导出的 UIKit MainViewController
 * 无缝挂载到 SwiftUI 视图层级中。
 */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
#endif

/**
 * iOS 宿主的主内容视图。
 *
 * 具备双模式自适应能力：
 * 1. 完整 App 运行模式（存在 Shared.framework）：通过 ComposeView 挂载完整的 Compose Multiplatform 页面；
 * 2. 独立 SPM 开发与测试模式（纯 Swift 环境）：渲染轻量级占位界面，确保无需 Gradle 编译即可直接运行 Swift 单元测试。
 */
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
