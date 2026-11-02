import Foundation

/// 依赖生命周期模式。
public enum DependencyScope: Sendable {
    /// 每次解析创建新实例
    case factory
    /// 单例生命周期，首次解析时创建并缓存
    case singleton
}

/// 内部依赖注册项封装。
private final class ServiceFactory<T>: @unchecked Sendable {
    let scope: DependencyScope
    let factory: @Sendable () -> T
    private var instance: T?
    private let lock = NSLock()

    init(scope: DependencyScope, factory: @escaping @Sendable () -> T) {
        self.scope = scope
        self.factory = factory
    }

    func resolve() -> T {
        switch scope {
        case .factory:
            return factory()
        case .singleton:
            lock.lock()
            defer { lock.unlock() }
            if let existing = instance {
                return existing
            }
            let created = factory()
            instance = created
            return created
        }
    }
}

/// 轻量级原生依赖注入中心。
/// 线程安全，严格兼容 Swift 6 并发模型，支持按协议抽象注册与 Fake 替换。
public final class DependencyContainer: @unchecked Sendable {
    public static let shared = DependencyContainer()

    private var factories: [String: Any] = [:]
    private let lock = NSRecursiveLock()

    public init() {}

    /// 注册服务提供者。
    /// - Parameters:
    ///   - type: 接口或类类型
    ///   - scope: 实例生命周期（默认为 singleton）
    ///   - factory: 构造闭包
    public func register<T>(
        _ type: T.Type,
        scope: DependencyScope = .singleton,
        factory: @escaping @Sendable () -> T
    ) {
        lock.lock()
        defer { lock.unlock() }
        let key = String(reflecting: type)
        factories[key] = ServiceFactory<T>(scope: scope, factory: factory)
    }

    /// 解析服务实例。
    /// - Parameter type: 接口或类类型
    /// - Returns: 注册的服务实例
    public func resolve<T>(_ type: T.Type = T.self) -> T {
        lock.lock()
        defer { lock.unlock() }
        let key = String(reflecting: type)
        guard let serviceFactory = factories[key] as? ServiceFactory<T> else {
            fatalError("CoreDI: 未找到类型 [\(key)] 的注册项，请确保在 AppDependencyGraph 中已完成装配。")
        }
        return serviceFactory.resolve()
    }

    /// 重置容器（单元测试前清理）。
    public func reset() {
        lock.lock()
        defer { lock.unlock() }
        factories.removeAll()
    }
}
