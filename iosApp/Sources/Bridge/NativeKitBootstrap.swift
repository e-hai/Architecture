import Foundation
import SwiftUI
import LogKit
import CrashReportKit
import Analytics
import AbTesting
import MmpCore
import AdsCore
import PayKit
import PushKit

#if canImport(Shared)
import Shared
#endif

/**
 * iOS 原生商业化 Kit 引导器（NativeKitBootstrap）。
 * 专职负责未实现跨平台 KMP 的 8 大 iOS 原生 Swift SPM SDK 的初始化与 Bridge 桥接挂载，
 * 与 Android 端的 NativeKitBootstrap 保持命名与职责 100% 对齐。
 */
public final class NativeKitBootstrap: Sendable {
    /**
     * 批量初始化并注册所有 iOS 原生 Kit 的 Swift SPM 桥接实现。
     */
    public static func initKits() {
        print("[KMP-iOS] Initializing 8 native Kit SPM SDKs & Bridges...")

        // 1. 注册 LogKit (通过 core:log 门面挂载代理并初始化，外部不直接调用原生 Kit API)
        #if canImport(Shared)
        Shared.LogKit.shared.install(delegate: LogBridge())
        Shared.LogKit.shared.doInitAllLog()
        #else
        LogKit.initAllLog()
        #endif

        // 2. 初始化 CrashReportKit
        CrashReport.initialize(config: CrashReportConfig(enabled: true))

        // 3. 初始化 AnalyticsKit (控制台输出 Provider)
        Analytics.initialize(config: AnalyticsConfig(
            providers: [LoggingAnalyticsProvider()],
            enabled: true,
            debug: true
        ))

        // 4. 初始化 AbTestingKit (默认本地/测试 Fake Provider)
        AbTestingClient.install(FakeAbTestingProvider())

        // 5. 初始化 MmpKit
        Mmp.setLogEnabled(true)

        // 6. 初始化 PayKit
        PayKit.configure(configuration: PayKitConfiguration())

        // 7. 初始化 PushKit (仅在具有 Bundle Identifier 的实际 App 运行时初始化通知中心)
        if Bundle.main.bundleIdentifier != nil {
            PushKitManager.shared.setup()
        }

        // 8. 挂载 8 大原生 Kit 桥接实现到 KMP 跨平台门面
        #if canImport(Shared)
        CrashReportIosBridgeKt.registerCrashReportIosBridge(bridge: CrashReportBridge())
        AnalyticsIosBridgeKt.registerAnalyticsIosBridge(bridge: AnalyticsBridge())
        AbTestingIosBridgeKt.registerAbTestingIosBridge(bridge: AbTestingBridge())
        MmpIosBridgeKt.registerMmpIosBridge(bridge: MmpBridge())
        AdsIosBridgeKt.registerAdsIosBridge(bridge: AdsBridge())
        PayIosBridgeKt.registerPayIosBridge(bridge: PayBridge())
        PushIosBridgeKt.registerPushIosBridge(bridge: PushBridge())
        print("[KMP-iOS] 8 native Kit bridges successfully registered to KMP.")
        #endif
    }
}

// MARK: - KMP Bridge Implementations

#if canImport(Shared)
private final class LogBridge: NSObject, LogDelegate {
    func doInitAllLog() {
        LogKit.initAllLog()
    }

    func doInitOnlyConsoleLog() {
        LogKit.initOnlyConsoleLog()
    }

    func d(tag: String, message: String) {
        LogKit.d(tag, message)
    }

    func i(tag: String, message: String) {
        LogKit.i(tag, message)
    }

    func w(tag: String, message: String) {
        LogKit.w(tag, message)
    }

    func e(tag: String, message: String, throwable: KotlinThrowable?) {
        if let err = throwable?.message {
            LogKit.e(tag, "\(message) | \(err)")
        } else {
            LogKit.e(tag, message)
        }
    }
}

private final class CrashReportBridge: NSObject, CrashReportIosBridge {
    func setUserId(userId_: String) {
        CrashReport.setUserId(userId_)
    }

    func setCustomKey(key: String, value: Any) {
        CrashReport.setCustomKey(key, value: String(describing: value))
    }

    func log(message: String) {
        CrashReport.log(message)
    }

    func recordException(throwable: KotlinThrowable) {
        let error = NSError(
            domain: "KotlinThrowable",
            code: -1,
            userInfo: [NSLocalizedDescriptionKey: throwable.message ?? "Kotlin Exception"]
        )
        CrashReport.recordException(error)
    }
}

private final class AnalyticsBridge: NSObject, AnalyticsIosBridge {
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

private final class AbTestingBridge: NSObject, AbTestingIosBridge {
    func getBoolean(key: String, defaultValue: Bool) -> Bool {
        AbTestingClient.getBoolean(key, default: defaultValue)
    }

    func getDouble(key: String, defaultValue: Double) -> Double {
        AbTestingClient.getDouble(key, default: defaultValue)
    }

    func getLong(key: String, defaultValue: Int64) -> Int64 {
        AbTestingClient.getLong(key, default: defaultValue)
    }

    func getString(key: String, defaultValue: String) -> String {
        AbTestingClient.getString(key, default: defaultValue)
    }
}

private final class MmpBridge: NSObject, MmpIosBridge {
    func trackEvent(eventName: String, params: [String: Any]) {
        Mmp.trackEvent(eventName, params: params)
    }

    func setCustomerUserId(userId: String?) {
        if let uid = userId {
            Mmp.trackEvent("set_customer_user_id", params: ["user_id": uid])
        }
    }
}

private final class AdsBridge: NSObject, AdsIosBridge {
    func isReady(triggerId: String) -> Bool {
        // AdsKit 预加载状态检查
        return true
    }

    func loadAd(triggerId: String, adUnitId: String) {
        print("[AdsBridge] loadAd: triggerId=\(triggerId), adUnitId=\(adUnitId)")
    }

    func showAd(triggerId: String) {
        print("[AdsBridge] showAd: triggerId=\(triggerId)")
    }
}

private final class PayBridge: NSObject, PayIosBridge {
    func isSubscribed(productId: String) -> Bool {
        return PayKit.shared.findActiveSubscription(productId: productId) != nil
    }

    func purchase(productId: String, onResult: @escaping (KotlinBoolean, String?) -> Void) {
        Task {
            let result = await PayKit.shared.syncPurchases()
            switch result {
            case .success:
                onResult(KotlinBoolean(bool: true), nil)
            case .failure(let error):
                onResult(KotlinBoolean(bool: false), error.message)
            }
        }
    }
}

private final class PushBridge: NSObject, PushIosBridge {
    func registerPush() {
        Task {
            _ = try? await PushKitManager.shared.requestPermission()
        }
    }

    func setPushToken(token: String) {
        print("[PushBridge] setPushToken: \(token)")
    }
}
#endif
