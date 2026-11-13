import Foundation
import MmpCore

#if canImport(MmpAppsFlyer)
import MmpAppsFlyer
#endif

#if canImport(AppsFlyerLib)
import AppsFlyerLib
#endif

#if canImport(Shared)
import Shared

/**
 * iOS 宿主深度链接（URL Scheme / Universal Links）转发桥接器。
 * 将原生 MmpKit Direct Deep Link API 严格封装在 Delegates 目录内，由 iOSApp 在系统回调入口调用。
 */
internal enum IosMmpBridge {
    static func handleOpenUrl(_ url: URL) {
        CoreLog.shared.d(tag: LogTags.shared.MMP, message: "[Mmp-iOS] Forwarding openURL: \(url.absoluteString)")
        Mmp.handleOpenUrl(url)
    }

    static func continueUserActivity(_ userActivity: NSUserActivity) {
        CoreLog.shared.d(
            tag: LogTags.shared.MMP,
            message: "[Mmp-iOS] Forwarding continueUserActivity: \(userActivity.webpageURL?.absoluteString ?? "nil")"
        )
        Mmp.continueUserActivity(userActivity)
    }
}

/**
 * iOS 归因统计委托实现（1:1 透传 MmpKit-iOS 原生 SDK）。
 */
internal final class IosMmpDelegate: NSObject, MmpDelegate, @unchecked Sendable {
    private let debug: Bool
    private let appId: String?
    private let lock = NSLock()
    private var attributionCallback: ((MmpAttribution) -> Void)?
    private var deepLinkCallback: ((MmpDeepLink) -> Void)?
    private var pendingDeepLink: MmpDeepLink?

    init(debug: Bool = true, appId: String? = nil) {
        self.debug = debug
        let trimmed = appId?.trimmingCharacters(in: .whitespacesAndNewlines)
        self.appId = (trimmed?.isEmpty == false) ? trimmed : nil
        super.init()
    }

    func doInit(appToken: String) {
        let trimmedToken = appToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedToken.isEmpty else {
            Mmp.setLogEnabled(debug)
            CoreLog.shared.w(tag: LogTags.shared.MMP, message: "[Mmp-iOS] APPSFLYER_KEY 为空，跳过 Mmp SDK 初始化")
            return
        }

        #if canImport(MmpAppsFlyer)
        Mmp.initialize(
            config: MmpConfig(
                appToken: trimmedToken,
                isDebug: debug,
                appId: appId,
                enableLog: debug
            ),
            client: AppsFlyerClient()
        )
        Mmp.setAttributionListener { [weak self] data in
            let mapped = Self.mapAttribution(data)
            CoreLog.shared.i(
                tag: LogTags.shared.MMP,
                message: "[Mmp-iOS] Attribution arrived: isOrganic=\(mapped.isOrganic), network=\(mapped.network ?? "nil"), campaign=\(mapped.campaign ?? "nil")"
            )
            self?.deliverAttribution(mapped)
        }
        Mmp.setDeepLinkListener { [weak self] result in
            let mapped = Self.mapDeepLink(result)
            CoreLog.shared.i(
                tag: LogTags.shared.MMP,
                message: "[Mmp-iOS] DeepLink resolved: value=\(mapped.deepLinkValue ?? "nil"), isDeferred=\(mapped.isDeferred)"
            )
            self?.deliverDeepLink(mapped)
        }
        #else
        Mmp.setLogEnabled(debug)
        CoreLog.shared.w(tag: LogTags.shared.MMP, message: "[Mmp-iOS] MmpAppsFlyer 未链接，仅开启日志模式")
        #endif
    }

    func trackEvent(eventName: String, params: [String: Any]) {
        Mmp.trackEvent(eventName, params: params)
    }

    func trackRevenue(revenue: Double, currency: String, eventName: String, params: [String: Any]) {
        Mmp.trackRevenue(
            MmpRevenue(
                revenue: revenue,
                currency: currency,
                eventName: eventName,
                params: params
            )
        )
    }

    func setCustomerUserId(userId: String?) {
        var syncedToAppsFlyer = false
        #if canImport(AppsFlyerLib)
        AppsFlyerLib.shared().customerUserID = userId
        syncedToAppsFlyer = true
        #endif
        let hasUserId = !(userId?.isEmpty ?? true)
        CoreLog.shared.d(
            tag: LogTags.shared.MMP,
            message: "[Mmp-iOS] setCustomerUserId synced=\(syncedToAppsFlyer), hasUserId=\(hasUserId)"
        )
    }

    func getAttribution() -> MmpAttribution? {
        Mmp.attribution.map(Self.mapAttribution)
    }

    func setAttributionListener(listener: ((MmpAttribution) -> Void)?) {
        lock.lock()
        attributionCallback = listener
        lock.unlock()

        if let listener, let cached = Mmp.attribution.map(Self.mapAttribution) {
            listener(cached)
        }
    }

    func setDeepLinkListener(listener: ((MmpDeepLink) -> Void)?) {
        lock.lock()
        deepLinkCallback = listener
        let pending = listener != nil ? pendingDeepLink : nil
        if pending != nil {
            pendingDeepLink = nil
        }
        lock.unlock()

        if let listener, let pending {
            listener(pending)
        }
    }

    private func deliverAttribution(_ attribution: MmpAttribution) {
        lock.lock()
        let callback = attributionCallback
        lock.unlock()
        callback?(attribution)
    }

    private func deliverDeepLink(_ deepLink: MmpDeepLink) {
        lock.lock()
        let callback = deepLinkCallback
        if callback == nil {
            pendingDeepLink = deepLink
        }
        lock.unlock()
        callback?(deepLink)
    }

    private static func mapAttribution(_ data: MmpCore.AttributionData) -> MmpAttribution {
        MmpAttribution(
            network: data.network,
            campaign: data.campaign,
            adGroup: data.adGroup,
            creative: data.creative,
            clickLabel: data.clickLabel,
            isOrganic: data.isOrganic
        )
    }

    private static func mapDeepLink(_ result: MmpCore.DeepLinkResult) -> MmpDeepLink {
        var stringParams: [String: String] = [:]
        for (key, value) in result.clickEvent {
            stringParams[key] = "\(value)"
        }
        return MmpDeepLink(
            deepLinkValue: result.deepLinkValue,
            params: stringParams,
            isDeferred: result.isDeferred
        )
    }
}
#endif
