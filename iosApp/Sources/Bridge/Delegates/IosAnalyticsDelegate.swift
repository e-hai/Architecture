import Analytics
import Foundation
import Shared

#if canImport(AnalyticsFirebase)
import AnalyticsFirebase
#endif

#if canImport(FirebaseCore)
import FirebaseCore
#endif

/**
 * iOS 数据埋点委托实现（1:1 透传 AnalyticsKit-iOS 原生 SDK）。
 */
internal final class IosAnalyticsDelegate: NSObject, AnalyticsDelegate, @unchecked Sendable {
    private let debug: Bool

    init(debug: Bool = true) {
        self.debug = debug
        super.init()
    }

    func doInit() {
        var providers: [AnalyticsProvider] = []
        if debug {
            providers.append(LoggingAnalyticsProvider())
        }

        #if canImport(AnalyticsFirebase)
        let firebaseReady: Bool
        #if canImport(FirebaseCore)
        firebaseReady = FirebaseApp.app() != nil
        #else
        firebaseReady = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil
        #endif
        if firebaseReady {
            providers.append(FirebaseAnalyticsProvider())
        } else {
            CoreLog.shared.w(
                tag: LogTags.shared.ANALYTICS,
                message: "[Analytics-iOS] GoogleService-Info.plist 未配置，跳过 FirebaseAnalyticsProvider"
            )
        }
        #endif

        Analytics.initialize(config: AnalyticsConfig(
            providers: providers,
            enabled: true,
            debug: debug
        ))
    }

    func logEvent(name: String, params: [String: Any]) {
        var analyticsParams: [String: AnalyticsValue] = [:]
        for (key, val) in params {
            if let str = val as? String {
                analyticsParams[key] = .string(str)
            } else if let b = val as? Bool {
                analyticsParams[key] = .bool(b)
            } else if let num = val as? NSNumber {
                analyticsParams[key] = .double(num.doubleValue)
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
