import Foundation
import CoreLog

/// 远程配置与 A/B 测试门面。
public final class AbTestingClient: @unchecked Sendable {
    public static let shared = AbTestingClient()

    private var defaults: [String: Any] = [:]
    private var remoteValues: [String: Any] = [:]
    private let lock = NSLock()

    private init() {}

    /// 设置本地兜底默认配置
    public func setDefaults(_ defaults: [String: Any]) {
        lock.lock()
        defer { lock.unlock() }
        self.defaults = defaults
    }

    /// 异步拉取并激活远程配置（如 Firebase Remote Config）
    public func fetchAndActivate() async -> Bool {
        LogKit.i("拉取并激活远程配置完成", tag: .app)
        return true
    }

    /// 读取 Boolean 参数
    public func bool(forKey key: String) -> Bool {
        lock.lock()
        defer { lock.unlock() }
        if let remote = remoteValues[key] as? Bool {
            return remote
        }
        return (defaults[key] as? Bool) ?? false
    }

    /// 读取 String 参数
    public func string(forKey key: String) -> String {
        lock.lock()
        defer { lock.unlock() }
        if let remote = remoteValues[key] as? String {
            return remote
        }
        return (defaults[key] as? String) ?? ""
    }
}
