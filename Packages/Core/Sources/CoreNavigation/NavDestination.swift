import SwiftUI

/// 跨模块类型安全路由目标协议。
/// 所有 Feature 暴露给外部的路由 Key 必须遵循此协议。
public protocol NavDestination: Hashable, Identifiable, Sendable {
    /// 唯一标识
    var id: String { get }
}

public extension NavDestination {
    var id: String {
        String(reflecting: self)
    }
}
