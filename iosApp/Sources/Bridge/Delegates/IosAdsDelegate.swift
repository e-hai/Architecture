import AdsKit
import Foundation
import Shared
import UIKit

/**
 * 内部线程安全的广告格式映射表（桥接 Kotlin `AdFormat` 与 Swift `AdsKit.AdsType` / Info.plist 默认配置）。
 */
private enum AdFormatKind: Sendable {
    case rewarded
    case interstitial
    case appOpen
    case banner

    init(_ format: AdFormat) {
        switch format {
        case .rewarded: self = .rewarded
        case .interstitial: self = .interstitial
        case .appOpen: self = .appOpen
        case .banner: self = .banner
        default: self = .rewarded
        }
    }

    init(triggerId: String) {
        self.init(AdsKeys.shared.defaultFormatFor(triggerId: triggerId))
    }

    var sdkType: AdsType {
        switch self {
        case .rewarded: return .rewarded
        case .interstitial: return .interstitial
        case .appOpen: return .splash
        case .banner: return .banner
        }
    }

    var infoPlistKey: String {
        switch self {
        case .rewarded: return "ADMOB_REWARDED_ID"
        case .interstitial: return "ADMOB_INTERSTITIAL_ID"
        case .appOpen: return "ADMOB_APP_OPEN_ID"
        case .banner: return "ADMOB_BANNER_ID"
        }
    }

    var displayTitle: String {
        switch self {
        case .rewarded: return "🎬 激励广告模拟展示"
        case .interstitial: return "🖼️ 插屏广告模拟展示"
        case .appOpen: return "🚀 开屏广告模拟展示"
        case .banner: return "📢 横幅广告模拟展示"
        }
    }
}

/**
 * iOS 广告聚合委托实现（1:1 透传 AdsKit-iOS 原生 SDK，支持多广告格式映射与独立底部 Banner 容器）。
 */
internal final class IosAdsDelegate: NSObject, AdsDelegate, @unchecked Sendable {
    private static let bannerOverlayTag = 94821

    private let debug: Bool
    private let defaultUnitIds: [AdFormatKind: String]
    private let lock = NSLock()
    private var loadedAds: [String: (entity: AdsEntity, kind: AdFormatKind)] = [:]
    @MainActor private var activeBannerEntity: AdsEntity?

    init(
        defaultBannerUnitId: String = "",
        defaultInterstitialUnitId: String = "",
        defaultRewardedUnitId: String = "",
        defaultAppOpenUnitId: String = "",
        debug: Bool = false
    ) {
        self.debug = debug
        self.defaultUnitIds = [
            .banner: defaultBannerUnitId,
            .interstitial: defaultInterstitialUnitId,
            .rewarded: defaultRewardedUnitId,
            .appOpen: defaultAppOpenUnitId
        ]
        super.init()
    }

    private func setLoadedAd(_ entity: AdsEntity, kind: AdFormatKind, for triggerId: String) {
        lock.lock()
        defer { lock.unlock() }
        loadedAds[triggerId] = (entity, kind)
    }

    private func takeLoadedAd(for triggerId: String) -> (entity: AdsEntity, kind: AdFormatKind)? {
        lock.lock()
        defer { lock.unlock() }
        return loadedAds.removeValue(forKey: triggerId)
    }

    private func removeLoadedAd(for triggerId: String) {
        lock.lock()
        defer { lock.unlock() }
        loadedAds.removeValue(forKey: triggerId)
    }

    private func resolveUnitId(explicit adUnitId: String, kind: AdFormatKind) -> String {
        let trimmedExplicit = adUnitId.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmedExplicit.isEmpty {
            return trimmedExplicit
        }
        let configuredDefault = (defaultUnitIds[kind] ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        if !configuredDefault.isEmpty {
            return configuredDefault
        }
        return (Bundle.main.infoDictionary?[kind.infoPlistKey] as? String ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    func doInit(apiKey: String) {
        let trimmedKey = apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedKey.isEmpty else {
            CoreLog.shared.w(tag: LogTags.shared.APP, message: "[Ads-iOS] AdsManager init skipped: empty ADMOB_APP_ID")
            return
        }
        let isDebug = debug
        Task { @MainActor in
            AdsKit.registerAllProviders()
            AdsManager.initialize(
                config: AdsProviderConfig(
                    providerType: .admob,
                    apiKey: trimmedKey
                ),
                logEnabled: isDebug,
                debugEnabled: isDebug,
                logHandler: { level, tag, message, error in
                    let formatted = "[\(tag)] \(message)\(error.map { " error=\($0.localizedDescription)" } ?? "")"
                    switch level {
                    case .debug:
                        CoreLog.shared.d(tag: LogTags.shared.APP, message: formatted)
                    case .warn:
                        CoreLog.shared.w(tag: LogTags.shared.APP, message: formatted)
                    case .error:
                        CoreLog.shared.e(tag: LogTags.shared.APP, message: formatted, throwable: nil)
                    }
                },
                onResult: { success in
                    CoreLog.shared.i(tag: LogTags.shared.APP, message: "[Ads-iOS] AdsManager initialized: \(success)")
                }
            )
        }
    }

    func loadAd(triggerId: String, adUnitId: String, format: AdFormat) {
        let kind = AdFormatKind(format)
        let resolvedUnitId = resolveUnitId(explicit: adUnitId, kind: kind)
        guard !resolvedUnitId.isEmpty else {
            CoreLog.shared.w(
                tag: LogTags.shared.ADS,
                message: "[Ads-iOS] loadAd skipped: empty adUnitId for trigger=\(triggerId), format=\(kind)"
            )
            return
        }

        Task { @MainActor [weak self] in
            print("[Ads-iOS] loadAd: triggerId=\(triggerId), unitId=\(resolvedUnitId), type=\(kind.sdkType.name)")
            let request = AdsRequest(
                triggerId: triggerId,
                adUnitId: resolvedUnitId,
                adType: kind.sdkType,
                providerType: .admob
            )
            let listener = CustomAdsListener(
                onLoaded: { [weak self] entity in
                    print("[Ads-iOS] onAdLoaded (\(kind.sdkType.name)): \(triggerId)")
                    self?.setLoadedAd(entity, kind: kind, for: triggerId)
                },
                onFailed: { error, code in
                    print("[Ads-iOS] onAdFailed (\(kind.sdkType.name)): \(triggerId) error=\(error) code=\(code ?? "")")
                },
                onClosed: { [weak self] in
                    print("[Ads-iOS] onAdClosed (\(kind.sdkType.name)): \(triggerId)")
                    self?.removeLoadedAd(for: triggerId)
                }
            )
            AdsManager.loadAd(request: request, adListener: listener)
        }
    }

    func isReady(triggerId: String) -> Bool {
        lock.lock()
        defer { lock.unlock() }
        return loadedAds[triggerId] != nil
    }

    func showAd(triggerId: String) {
        let cached = takeLoadedAd(for: triggerId)
        let kind = cached?.kind ?? AdFormatKind(triggerId: triggerId)
        let entity = cached?.entity

        Task { @MainActor [weak self] in
            print("[Ads-iOS] showAd: triggerId=\(triggerId), type=\(kind.sdkType.name)")
            guard let topVC = self?.getTopViewController() else {
                print("[Ads-iOS] showAd failed: no topViewController")
                return
            }

            if let entity {
                if kind == .banner {
                    // 使用独立悬浮子容器承载 Banner，防止 AdmobProviderAdapter 清空 Compose 根视图
                    self?.activeBannerEntity?.destroy()
                    self?.activeBannerEntity = entity
                    if let bannerContainer = self?.getOrCreateBannerContainer(in: topVC.view) {
                        entity.show(from: topVC, in: bannerContainer)
                    }
                } else {
                    entity.show(from: topVC)
                }
            } else {
                self?.showFallbackAdModal(from: topVC, triggerId: triggerId, kind: kind)
            }
        }
    }

    func openDebug() {
        Task { @MainActor in
            guard let topVC = self.getTopViewController() else {
                CoreLog.shared.w(tag: LogTags.shared.APP, message: "[Ads-iOS] openDebug failed: no topViewController")
                return
            }
            AdsManager.openDebug(from: topVC)
        }
    }

    @MainActor
    private func getOrCreateBannerContainer(in rootView: UIView) -> UIView {
        if let existing = rootView.viewWithTag(Self.bannerOverlayTag) {
            rootView.bringSubviewToFront(existing)
            return existing
        }
        let container = UIView()
        container.tag = Self.bannerOverlayTag
        container.backgroundColor = .clear
        container.translatesAutoresizingMaskIntoConstraints = false
        rootView.addSubview(container)
        let minHeight = container.heightAnchor.constraint(greaterThanOrEqualToConstant: 50)
        minHeight.priority = .defaultHigh
        NSLayoutConstraint.activate([
            container.centerXAnchor.constraint(equalTo: rootView.centerXAnchor),
            container.widthAnchor.constraint(equalTo: rootView.safeAreaLayoutGuide.widthAnchor),
            container.bottomAnchor.constraint(equalTo: rootView.safeAreaLayoutGuide.bottomAnchor),
            minHeight
        ])
        return container
    }

    @MainActor
    private func showFallbackAdModal(from viewController: UIViewController, triggerId: String, kind: AdFormatKind) {
        let alert = UIAlertController(
            title: kind.displayTitle,
            message: "【广告单元触发点: \(triggerId) / \(kind.sdkType.name)】\n\n当前处于测试环境或真机暂未缓存到远端 AdMob 广告。若在生产环境将展示真实广告。",
            preferredStyle: .alert
        )
        let primaryLabel = (kind == .rewarded) ? "完成观看 (领取奖励)" : "确认"
        alert.addAction(UIAlertAction(title: primaryLabel, style: .default) { _ in
            print("[Ads-iOS] Fallback Ad complete (\(kind.sdkType.name)) for trigger=\(triggerId)")
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

    override func onAdLoaded(_ entity: AdsEntity) {
        onLoadedClosure(entity)
    }

    override func onAdFailedToLoad(error: String, errorCode: String?) {
        onFailedClosure(error, errorCode)
    }

    override func onAdClosed() {
        onClosedClosure()
    }
}
