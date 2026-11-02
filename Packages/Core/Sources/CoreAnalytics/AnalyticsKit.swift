import Foundation
import CoreLog

/// 统一数据统计打点 Provider 协议。
public protocol AnalyticsProvider: Sendable {
    func logEvent(name: String, parameters: [String: Any]?)
    func setUserId(_ userId: String?)
    func setUserProperty(name: String, value: String?)
}

/// 默认本地控制台打点 Provider。
public final class ConsoleAnalyticsProvider: AnalyticsProvider {
    public init() {}

    public func logEvent(name: String, parameters: [String: Any]?) {
        LogKit.i("📊 打点事件: [\(name)] 参数: \(parameters ?? [:])", tag: .analytics)
    }

    public func setUserId(_ userId: String?) {
        LogKit.i("📊 设置用户ID: \(userId ?? "nil")", tag: .analytics)
    }

    public func setUserProperty(name: String, value: String?) {
        LogKit.i("📊 设置用户属性: \(name)=\(value ?? "nil")", tag: .analytics)
    }
}

/// AnalyticsKit 全局统计门面。
public final class Analytics: @unchecked Sendable {
    public static let shared = Analytics()

    private var providers: [AnalyticsProvider] = [ConsoleAnalyticsProvider()]
    private let lock = NSLock()

    private init() {}

    /// 配置打点提供商列表（如 Firebase / AppsFlyer）
    public func setProviders(_ providers: [AnalyticsProvider]) {
        lock.lock()
        defer { lock.unlock() }
        self.providers = providers
    }

    /// 上报事件
    public static func logEvent(_ name: String, parameters: [String: Any]? = nil) {
        shared.lock.lock()
        let currentProviders = shared.providers
        shared.lock.unlock()

        for provider in currentProviders {
            provider.logEvent(name: name, parameters: parameters)
        }
    }

    /// 设置用户 ID
    public static func setUserId(_ userId: String?) {
        shared.lock.lock()
        let currentProviders = shared.providers
        shared.lock.unlock()

        for provider in currentProviders {
            provider.setUserId(userId)
        }
    }
}
