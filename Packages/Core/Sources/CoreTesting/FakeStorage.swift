import Foundation
import CoreStorage

/// 单元测试专用内存 Fake 键值存储实现。
public final class FakeStorage: KeyValueStorageProtocol, @unchecked Sendable {
    private var memory: [String: Any] = [:]
    private let lock = NSLock()

    public init() {}

    public func string(forKey key: String) -> String? {
        lock.lock()
        defer { lock.unlock() }
        return memory[key] as? String
    }

    public func set(_ value: String?, forKey key: String) {
        lock.lock()
        defer { lock.unlock() }
        memory[key] = value
    }

    public func bool(forKey key: String) -> Bool {
        lock.lock()
        defer { lock.unlock() }
        return (memory[key] as? Bool) ?? false
    }

    public func set(_ value: Bool, forKey key: String) {
        lock.lock()
        defer { lock.unlock() }
        memory[key] = value
    }

    public func integer(forKey key: String) -> Int {
        lock.lock()
        defer { lock.unlock() }
        return (memory[key] as? Int) ?? 0
    }

    public func set(_ value: Int, forKey key: String) {
        lock.lock()
        defer { lock.unlock() }
        memory[key] = value
    }

    public func data(forKey key: String) -> Data? {
        lock.lock()
        defer { lock.unlock() }
        return memory[key] as? Data
    }

    public func set(_ value: Data?, forKey key: String) {
        lock.lock()
        defer { lock.unlock() }
        memory[key] = value
    }

    public func object<T: Codable & Sendable>(_ type: T.Type, forKey key: String) -> T? {
        guard let data = data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(T.self, from: data)
    }

    public func setObject<T: Codable & Sendable>(_ value: T?, forKey key: String) {
        if let value = value, let data = try? JSONEncoder().encode(value) {
            set(data, forKey: key)
        } else {
            removeObject(forKey: key)
        }
    }

    public func removeObject(forKey key: String) {
        lock.lock()
        defer { lock.unlock() }
        memory.removeValue(forKey: key)
    }
}
