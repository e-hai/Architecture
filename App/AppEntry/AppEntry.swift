import SwiftUI
import AppLib

/// iOS 应用程序主入口。
@main
struct AppEntry: App {
    init() {
        AppDependencyGraph.registerAll()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
