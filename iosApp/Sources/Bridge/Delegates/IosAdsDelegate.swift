import AdsKit
import Foundation

#if canImport(Shared)
import Shared
import UIKit

/**
 * iOS 广告聚合委托实现（1:1 透传 AdsKit-iOS 原生 SDK）。
 */
internal final class IosAdsDelegate: NSObject, AdsDelegate, @unchecked Sendable {
    private let debug: Bool
    private let lock = NSLock()
    private var loadedAds: [String: AdsEntity] = [:]

    init(debug: Bool = false) {
        self.debug = debug
        super.init()
    }

    private func setLoadedAd(_ entity: AdsEntity, for triggerId: String) {
        lock.lock()
        defer { lock.unlock() }
        loadedAds[triggerId] = entity
    }

    private func takeLoadedAd(for triggerId: String) -> AdsEntity? {
        lock.lock()
        defer { lock.unlock() }
        return loadedAds.removeValue(forKey: triggerId)
    }

    private func removeLoadedAd(for triggerId: String) {
        lock.lock()
        defer { lock.unlock() }
        loadedAds.removeValue(forKey: triggerId)
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
                }
            ) { success in
                CoreLog.shared.i(tag: LogTags.shared.APP, message: "[Ads-iOS] AdsManager initialized: \(success)")
            }
        }
    }

    func loadAd(triggerId: String, adUnitId: String) {
        Task { @MainActor [weak self] in
            print("[Ads-iOS] loadAd: triggerId=\(triggerId), adUnitId=\(adUnitId)")
            let configuredRewarded = Bundle.main.infoDictionary?["ADMOB_REWARDED_ID"] as? String ?? ""
            let unitId = adUnitId.isEmpty ? configuredRewarded : adUnitId
            guard !unitId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                CoreLog.shared.w(tag: LogTags.shared.APP, message: "[Ads-iOS] loadAd skipped: empty adUnitId for trigger=\(triggerId)")
                return
            }
            let request = AdsRequest(
                triggerId: triggerId,
                adUnitId: unitId,
                adType: .rewarded,
                providerType: .admob
            )
            let listener = CustomAdsListener(
                onLoaded: { [weak self] entity in
                    print("[Ads-iOS] onAdLoaded: \(triggerId)")
                    self?.setLoadedAd(entity, for: triggerId)
                },
                onFailed: { error, code in
                    print("[Ads-iOS] onAdFailed: \(triggerId) error=\(error) code=\(code ?? "")")
                },
                onClosed: { [weak self] in
                    print("[Ads-iOS] onAdClosed: \(triggerId)")
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
        Task { @MainActor in
            print("[Ads-iOS] showAd: triggerId=\(triggerId)")
            guard let topVC = self.getTopViewController() else {
                print("[Ads-iOS] showAd failed: no topViewController")
                return
            }

            if let entity = self.takeLoadedAd(for: triggerId) {
                entity.show(from: topVC)
            } else {
                self.showFallbackAdModal(from: topVC, triggerId: triggerId)
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
#endif
