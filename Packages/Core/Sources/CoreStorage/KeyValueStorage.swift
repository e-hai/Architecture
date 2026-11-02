import Foundation

/// 本地键值存储协议。
public protocol KeyValueStorageProtocol: Sendable {
    func string(forKey key: String) -> String?
    func set(_ value: String?, forKey key: String)

    func bool(forKey key: String) -> Bool
    func set(_ value: Bool, forKey key: String)

    func integer(forKey key: String) -> Int
    func set(_ value: Int, forKey key: String)

    func data(forKey key: String) -> Data?
    func set(_ value: Data?, forKey key: String)

    func object<T: Codable & Sendable>(_ type: T.Type, forKey key: String) -> T?
    func setObject<T: Codable & Sendable>(_ value: T?, forKey key: String)

    func removeObject(forKey key: String)
}

/// 默认 UserDefaults 封装实现。
/// UserDefaults 底层本身是线程安全的，此处标记 @unchecked Sendable。
public final class UserDefaultsStorage: KeyValueStorageProtocol, @unchecked Sendable {
    private let defaults: UserDefaults

    public init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    public func string(forKey key: String) -> String? {
        defaults.string(forKey: key)
    }

    public func set(_ value: String?, forKey key: String) {
        defaults.set(value, forKey: key)
    }

    public func bool(forKey key: String) -> Bool {
        defaults.bool(forKey: key)
    }

    public func set(_ value: Bool, forKey key: String) {
        defaults.set(value, forKey: key)
    }

    public func integer(forKey key: String) -> Int {
        defaults.integer(forKey: key)
    }

    public func set(_ value: Int, forKey key: String) {
        defaults.set(value, forKey: key)
    }

    public func data(forKey key: String) -> Data? {
        defaults.data(forKey: key)
    }

    public func set(_ value: Data?, forKey key: String) {
        defaults.set(value, forKey: key)
    }

    public func object<T: Codable & Sendable>(_ type: T.Type, forKey key: String) -> T? {
        guard let data = defaults.data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(T.self, from: data)
    }

    public func setObject<T: Codable & Sendable>(_ value: T?, forKey key: String) {
        if let value = value, let data = try? JSONEncoder().encode(value) {
            defaults.set(data, forKey: key)
        } else {
            defaults.removeObject(forKey: key)
        }
    }

    public func removeObject(forKey key: String) {
        defaults.removeObject(forKey: key)
    }
}
