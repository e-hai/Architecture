import Foundation

/// 依赖注入属性包装器。
/// 简化业务代码中对 DependencyContainer 实例的获取。
@propertyWrapper
public struct Injected<T> {
    private let container: DependencyContainer
    private var resolvedValue: T?

    public init(container: DependencyContainer = .shared) {
        self.container = container
    }

    public var wrappedValue: T {
        mutating get {
            if let value = resolvedValue {
                return value
            }
            let value = container.resolve(T.self)
            resolvedValue = value
            return value
        }
    }
}
