import SwiftUI
import CoreLog

/// 全局统一路由管理器。
/// 基于 iOS 16+ NavigationPath 驱动，支持跨模块类型安全路由跳转、返回与模态弹窗调度。
@MainActor
public final class AppRouter: ObservableObject {
    /// 导航栈路径
    @Published public var path = NavigationPath()

    public init() {}

    /// 导航推入新页面
    public func navigate(to destination: any NavDestination) {
        LogKit.d("Navigating to: \(destination.id)", tag: .navigation)
        path.append(destination)
    }

    /// 返回上一级页面
    public func pop() {
        guard !path.isEmpty else { return }
        LogKit.d("Popping top destination", tag: .navigation)
        path.removeLast()
    }

    /// 返回至根页面
    public func popToRoot() {
        LogKit.d("Popping to root", tag: .navigation)
        path = NavigationPath()
    }
}
