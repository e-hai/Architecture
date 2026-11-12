import Analytics
import Foundation

#if canImport(Shared)
import Shared

/**
 * iOS 数据埋点委托实现（1:1 透传 AnalyticsKit-iOS 原生 SDK）。
 */
internal final class IosAnalyticsDelegate: NSObject, AnalyticsDelegate {
    func doInit() {
        Analytics.initialize(config: AnalyticsConfig(
            providers: [LoggingAnalyticsProvider()],
            enabled: true,
            debug: true
        ))
    }

    func logEvent(name: String, params: [String: Any]) {
        var analyticsParams: [String: AnalyticsValue] = [:]
        for (key, val) in params {
            if let str = val as? String {
                analyticsParams[key] = .string(str)
            } else if let num = val as? NSNumber {
                analyticsParams[key] = .double(num.doubleValue)
            } else if let b = val as? Bool {
                analyticsParams[key] = .bool(b)
            } else {
                analyticsParams[key] = .string(String(describing: val))
            }
        }
        Analytics.logEvent(name, params: analyticsParams)
    }

    func setUserId(userId: String?) {
        Analytics.setUserId(userId)
    }

    func setUserProperty(name: String, value: String?) {
        Analytics.setUserProperty(name: name, value: value)
    }
}
#endif
