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
 * 专职负责 8 大 iOS 原生 Swift SPM SDK 的委托实现与跨平台门面挂载，
 * 与 Android 端的 NativeKitBootstrap 保持 1:1 对称与相同设计范式。
 */
public final class NativeKitBootstrap: Sendable {
    /**
     * 批量初始化并注册所有 iOS 原生 Kit 的 Swift SPM 委托实现至 Core 跨平台门面。
     */
    public static func initKits() {
        print("[KMP-iOS] Initializing 8 native Kit SPM SDKs & Delegates...")

        #if canImport(Shared)
        // 1. CoreLog (日志)
        CoreLog.shared.install(delegate: IosLogDelegate())
        CoreLog.shared.doInitAllLog()

        // 2. CoreCrashReport (崩溃采集)
        CoreCrashReport.shared.install(delegate: IosCrashReportDelegate())
        CoreCrashReport.shared.doInit()

        // 3. CoreAnalytics (数据埋点)
        CoreAnalytics.shared.install(delegate: IosAnalyticsDelegate())
        CoreAnalytics.shared.doInit()

        // 4. CoreAbTesting (A/B 测试 & Remote Config)
        CoreAbTesting.shared.install(delegate: IosAbTestingDelegate())
        CoreAbTesting.shared.doInit()

        // 5. CoreMmp (归因统计)
        CoreMmp.shared.install(delegate: IosMmpDelegate())
        CoreMmp.shared.doInit(appToken: "dummy_appsflyer_key")

        // 6. CoreAds (广告聚合)
        CoreAds.shared.install(delegate: IosAdsDelegate())
        CoreAds.shared.doInit(apiKey: "ca-app-pub-3940256099942544~3347511713")

        // 7. CorePay (支付结算)
        CorePay.shared.install(delegate: IosPayDelegate())
        CorePay.shared.doInit()

        // 8. CorePush (推送通知)
        CorePush.shared.install(delegate: IosPushDelegate())
        CorePush.shared.doInit(registerFcm: false)

        print("[KMP-iOS] 8 native Kit delegates successfully installed and initialized.")
        #else
        // CLI 测试环境兜底（当未链接 Shared.framework 时）
        LogKit.initAllLog()
        CrashReport.initialize(config: CrashReportConfig(enabled: true))
        Analytics.initialize(config: AnalyticsConfig(
            providers: [LoggingAnalyticsProvider()],
            enabled: true,
            debug: true
        ))
        AbTestingClient.install(FakeAbTestingProvider())
        Mmp.setLogEnabled(true)
        PayKit.configure(configuration: PayKitConfiguration())
        if Bundle.main.bundleIdentifier != nil {
            PushKitManager.shared.setup()
        }
        #endif
    }
}

// MARK: - iOS Native Kit Delegates (1:1 Symmetric with Android Android*Delegates)

#if canImport(Shared)
/**
 * iOS 日志委托实现。
 */
private final class IosLogDelegate: NSObject, LogDelegate {
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

/**
 * iOS 崩溃采集委托实现。
 */
private final class IosCrashReportDelegate: NSObject, CrashReportDelegate {
    func doInit() {
        CrashReport.initialize(config: CrashReportConfig(enabled: true))
    }

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

/**
 * iOS 数据埋点委托实现。
 */
private final class IosAnalyticsDelegate: NSObject, AnalyticsDelegate {
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

/**
 * iOS A/B 测试委托实现。
 */
private final class IosAbTestingDelegate: NSObject, AbTestingDelegate {
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

/**
 * iOS 归因统计委托实现。
 */
private final class IosMmpDelegate: NSObject, MmpDelegate {
    func doInit(appToken: String) {
        Mmp.setLogEnabled(true)
    }

    func trackEvent(eventName: String, params: [String: Any]) {
        Mmp.trackEvent(eventName, params: params)
    }

    func setCustomerUserId(userId: String?) {
        if let uid = userId {
            Mmp.trackEvent("set_customer_user_id", params: ["user_id": uid])
        }
    }
}

/**
 * iOS 广告聚合委托实现。
 */
private final class IosAdsDelegate: NSObject, AdsDelegate {
    func doInit(apiKey: String) {
        // iOS AdsKit 初始化逻辑
    }

    func loadAd(triggerId: String, adUnitId: String) {
        print("[Ads-iOS] loadAd: triggerId=\(triggerId), adUnitId=\(adUnitId)")
    }

    func isReady(triggerId: String) -> Bool {
        return true
    }

    func showAd(triggerId: String) {
        print("[Ads-iOS] showAd: triggerId=\(triggerId)")
    }
}

/**
 * iOS 支付结算委托实现。
 */
private final class IosPayDelegate: NSObject, PayDelegate {
    func doInit() {
        PayKit.configure(configuration: PayKitConfiguration())
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

    func isSubscribed(productId: String) -> Bool {
        return PayKit.shared.findActiveSubscription(productId: productId) != nil
    }
}

/**
 * iOS 推送通知委托实现。
 */
private final class IosPushDelegate: NSObject, PushDelegate {
    func doInit(registerFcm: Bool) {
        if Bundle.main.bundleIdentifier != nil {
            PushKitManager.shared.setup()
        }
    }

    func registerPush() {
        Task {
            _ = try? await PushKitManager.shared.requestPermission()
        }
    }

    func setPushToken(token: String) {
        print("[IosPushDelegate] setPushToken: \(token)")
    }
}
#endif
