import Foundation
import SwiftUI
import LogKit
import CrashReportKit
import Analytics
import AbTesting
import MmpCore
import AdsKit
import PayKit
import PushKit
import VideoKit

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
     *
     * @param admobAppId AdMob 应用 ID (来自 Info.plist GADApplicationIdentifier)
     * @param appsFlyerKey AppsFlyer Token (来自 Info.plist APPSFLYER_KEY)
     * @param debug 是否开启调试模式
     */
    public static func initKits(
        admobAppId: String = "",
        appsFlyerKey: String = "",
        debug: Bool = true
    ) {
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
        CoreMmp.shared.doInit(appToken: appsFlyerKey)

        // 6. CoreAds (广告聚合)
        CoreAds.shared.install(delegate: IosAdsDelegate())
        CoreAds.shared.doInit(apiKey: admobAppId)

        // 7. CorePay (支付结算)
        CorePay.shared.install(delegate: IosPayDelegate())
        CorePay.shared.doInit()

        // 8. CorePush (推送通知)
        CorePush.shared.install(delegate: IosPushDelegate())
        CorePush.shared.doInit(registerFcm: false)

        // 9. CoreVideo (视频播放与缓存)
        CoreVideo.shared.install(delegate: IosVideoDelegate())
        CoreVideo.shared.doInit()

        // 10. CorePag (PAG 动效渲染)
        CorePag.shared.doInit()

        print("[KMP-iOS] Native Kit delegates successfully installed and initialized.")
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
        _ = VideoCacheManager.shared
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

    func doInitOnlyDiskLog() {
        LogKit.initOnlyDiskLog()
    }

    func v(tag: String, message: String) {
        LogKit.v(tag, message)
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

    func wtf(tag: String, message: String) {
        LogKit.wtf(tag, message)
    }

    func json(tag: String, json: String) {
        LogKit.json(tag, json)
    }

    func xml(tag: String, xml: String) {
        LogKit.xml(tag, xml)
    }

    func readLog(callback: @escaping ([String]) -> Void) {
        LogKit.readLog { lines in
            callback(lines)
        }
    }

    func showLogUi() {
        DispatchQueue.main.async {
            guard let topVC = self.getTopViewController() else {
                LogKit.w("App", "No active UIViewController found to present LogKit UI")
                return
            }
            LogKit.showLogUI(from: topVC)
        }
    }

    @MainActor
    private func getTopViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }) ?? UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene }).first,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }) ?? windowScene.windows.first,
              var topController = window.rootViewController else {
            return nil
        }
        while let presented = topController.presentedViewController {
            topController = presented
        }
        return topController
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
private final class IosAdsDelegate: NSObject, AdsDelegate, @unchecked Sendable {
    @MainActor private var loadedAds: [String: AdsEntity] = [:]

    func doInit(apiKey: String) {
        Task { @MainActor in
            AdsKit.registerAllProviders()
            let admobAppId = apiKey.isEmpty ? "ca-app-pub-3940256099942544~1458002511" : apiKey
            AdsManager.initialize(
                config: AdsProviderConfig(
                    providerType: .admob,
                    apiKey: admobAppId
                )
            ) { success in
                print("[Ads-iOS] AdsManager initialized: \(success)")
            }
        }
    }

    func loadAd(triggerId: String, adUnitId: String) {
        Task { @MainActor in
            print("[Ads-iOS] loadAd: triggerId=\(triggerId), adUnitId=\(adUnitId)")
            let configuredRewarded = Bundle.main.infoDictionary?["ADMOB_REWARDED_ID"] as? String ?? ""
            let unitId = adUnitId.isEmpty ? configuredRewarded : adUnitId
            let request = AdsRequest(
                triggerId: triggerId,
                adUnitId: unitId,
                adType: .rewarded,
                providerType: .admob
            )
            let listener = CustomAdsListener(
                onLoaded: { [weak self] entity in
                    print("[Ads-iOS] onAdLoaded: \(triggerId)")
                    self?.loadedAds[triggerId] = entity
                },
                onFailed: { error, code in
                    print("[Ads-iOS] onAdFailed: \(triggerId) error=\(error) code=\(code ?? "")")
                },
                onClosed: { [weak self] in
                    print("[Ads-iOS] onAdClosed: \(triggerId)")
                    self?.loadedAds.removeValue(forKey: triggerId)
                }
            )
            AdsManager.loadAd(request: request, adListener: listener)
        }
    }

    func isReady(triggerId: String) -> Bool {
        return true
    }

    func showAd(triggerId: String) {
        Task { @MainActor in
            print("[Ads-iOS] showAd: triggerId=\(triggerId)")
            guard let topVC = self.getTopViewController() else {
                print("[Ads-iOS] showAd failed: no topViewController")
                return
            }

            if let entity = self.loadedAds[triggerId] {
                entity.show(from: topVC)
            } else {
                // 当处于模拟器环境或尚未成功从网络预加载到真实广告时，弹出全屏模拟广告弹窗以便直观验证链路
                self.showFallbackAdModal(from: topVC, triggerId: triggerId)
            }
        }
    }

    @MainActor
    private func showFallbackAdModal(from viewController: UIViewController, triggerId: String) {
        let alert = UIAlertController(
            title: "🎬 激励广告模拟展示",
            message: "【广告单元触发点: \(triggerId)】\n\n当前处于测试环境或真机暂未缓存到远端 AdMob 广告。若在生产环境将展示真实全屏视频；点击「完成观看」可模拟发放激励。",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "完成观看 (领取奖励)", style: .default) { _ in
            print("[Ads-iOS] Fallback Ad: User earned reward for trigger=\(triggerId)")
        })
        alert.addAction(UIAlertAction(title: "关闭", style: .cancel))
        viewController.present(alert, animated: true)
    }

    @MainActor
    private func getTopViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive || $0.activationState == .foregroundInactive }),
            let root = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController else {
            return nil
        }
        var top = root
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }
}

@MainActor
private final class CustomAdsListener: AdCallback {
    private let onLoadedClosure: (AdsEntity) -> Void
    private let onFailedClosure: (String, String?) -> Void
    private let onClosedClosure: () -> Void

    init(
        onLoaded: @escaping (AdsEntity) -> Void,
        onFailed: @escaping (String, String?) -> Void,
        onClosed: @escaping () -> Void
    ) {
        self.onLoadedClosure = onLoaded
        self.onFailedClosure = onFailed
        self.onClosedClosure = onClosed
        super.init()
    }

    override func onAdLoaded(_ ad: AdsEntity) {
        onLoadedClosure(ad)
    }

    override func onAdFailedToLoad(error: String, errorCode: String?) {
        onFailedClosure(error, errorCode)
    }

    override func onAdClosed() {
        onClosedClosure()
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

/**
 * iOS 视频播放与缓存委托实现。
 * 严格下沉封装 VideoKit-iOS 原生 SDK 调用（VideoCacheManager / VodProgressStore）。
 */
private final class IosVideoDelegate: NSObject, VideoDelegate {
    func doInit() {
        _ = VideoCacheManager.shared
        print("[IosVideoDelegate] VideoCacheManager initialized.")
    }

    func preload(url: String, bytes: Int64) {
        VideoCacheManager.shared.preload(urlString: url, bytes: bytes)
    }

    func preloadBidirectional(prevUrl: String?, nextUrl: String?, bytes: Int64) {
        VideoCacheManager.shared.preloadBidirectional(prevUrl: prevUrl, nextUrl: nextUrl, bytes: bytes)
    }

    func getCachedBytes(url: String) -> Int64 {
        return VideoCacheManager.shared.getCachedBytes(for: url)
    }

    func isCached(url: String, minBytes: Int64) -> Bool {
        return VideoCacheManager.shared.isCached(for: url, minBytes: minBytes)
    }

    func clearCache() {
        VideoCacheManager.shared.clearAllCache()
        print("[IosVideoDelegate] VideoCacheManager cache cleared.")
    }

    func saveProgress(videoId: String, positionMs: Int64, durationMs: Int64) {
        VodProgressStore.shared.saveProgress(for: videoId, positionSec: Double(positionMs) / 1000.0)
    }

    func getProgress(videoId: String) -> Int64 {
        let seconds = VodProgressStore.shared.getProgress(for: videoId)
        return Int64(seconds * 1000.0)
    }

    func clearProgress(videoId: String) {
        VodProgressStore.shared.clearProgress(for: videoId)
        print("[IosVideoDelegate] VodProgressStore cleared for: \(videoId)")
    }
}
#endif
