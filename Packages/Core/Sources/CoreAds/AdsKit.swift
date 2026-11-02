import Foundation
import CoreLog

/// 广告类型枚举。
public enum AdsType: String, Sendable {
    case banner = "banner"
    case interstitial = "interstitial"
    case rewarded = "rewarded"
}

/// 统一广告管理器门面。
public final class AdsManager: @unchecked Sendable {
    public static let shared = AdsManager()

    private var isInitialized = false
    private let lock = NSLock()

    private init() {}

    /// 初始化广告 SDK
    public func initialize() {
        lock.lock()
        defer { lock.unlock() }
        isInitialized = true
        LogKit.i("AdsKit 初始化完成", tag: .ads)
    }

    /// 加载并展示插屏广告
    public func showInterstitial(adUnitId: String, completion: (@Sendable (Bool) -> Void)? = nil) {
        LogKit.d("请求展示插屏广告: \(adUnitId)", tag: .ads)
        // 真实业务中桥接 Google AdMob / AppLovin SDK
        completion?(true)
    }

    /// 加载并展示激励视频广告
    public func showRewarded(adUnitId: String, onUserEarnedReward: (@Sendable (Int) -> Void)? = nil) {
        LogKit.d("请求展示激励视频广告: \(adUnitId)", tag: .ads)
        // 模拟完成奖励
        onUserEarnedReward?(1)
    }
}
