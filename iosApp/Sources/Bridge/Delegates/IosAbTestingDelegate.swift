import AbTesting
import Foundation

#if canImport(Shared)
import Shared

/**
 * iOS A/B 测试委托实现（1:1 透传 AbTestingKit-iOS 原生 SDK）。
 */
internal final class IosAbTestingDelegate: NSObject, AbTestingDelegate {
    func doInit() {
        AbTestingClient.install(FakeAbTestingProvider())
    }

    func getBoolean(key: String, defaultValue: Bool) -> Bool {
        AbTestingClient.getBoolean(key, default: defaultValue)
    }

    func getString(key: String, defaultValue: String) -> String {
        AbTestingClient.getString(key, default: defaultValue)
    }

    func getLong(key: String, defaultValue: Int64) -> Int64 {
        Int64(AbTestingClient.getInt(key, default: Int(defaultValue)))
    }

    func getDouble(key: String, defaultValue: Double) -> Double {
        AbTestingClient.getDouble(key, default: defaultValue)
    }
}
#endif
