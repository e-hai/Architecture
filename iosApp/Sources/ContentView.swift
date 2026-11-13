import Shared
import SwiftUI

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

/**
 * iOS 宿主的主内容视图。
 * 通过 ComposeView 挂载完整的 Compose Multiplatform 页面。
 */
public struct ContentView: View {
    public init() {}

    public var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
