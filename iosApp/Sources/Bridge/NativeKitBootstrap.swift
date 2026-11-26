import Foundation
import Shared
import SwiftUI

#if canImport(FirebaseCore)
import FirebaseCore
#endif

/**
 * iOS 宿主原生 Kit 分阶段初始化参数配置（与 Android `AndroidKitBootstrapConfig` 1:1 对称）。
 */
public struct IosKitBootstrapConfig: Sendable {
    public let admobAppId: String
    public let admobBannerId: String
    public let admobInterstitialId: String
    public let admobRewardedId: String
    public let admobAppOpenId: String
    public let appsFlyerKey: String
    public let appsFlyerAppId: String
    public let paySubsIds: String
    public let payConsumableIds: String
    public let payNonConsumableIds: String
    public let analyticsDebug: Bool?
    public let remoteConfigMinFetchIntervalSeconds: TimeInterval?
    public let registerPushAtLaunch: Bool?
    public let deferCrashReport: Bool
    public let debug: Bool

    public init(
        admobAppId: String = "",
        admobBannerId: String = "",
        admobInterstitialId: String = "",
        admobRewardedId: String = "",
        admobAppOpenId: String = "",
        appsFlyerKey: String = "",
        appsFlyerAppId: String = "",
        paySubsIds: String = "",
        payConsumableIds: String = "",
        payNonConsumableIds: String = "",
        analyticsDebug: Bool? = nil,
        remoteConfigMinFetchIntervalSeconds: TimeInterval? = nil,
        registerPushAtLaunch: Bool? = nil,
        deferCrashReport: Bool = false,
        debug: Bool = true
    ) {
        self.admobAppId = admobAppId
        self.admobBannerId = admobBannerId
        self.admobInterstitialId = admobInterstitialId
        self.admobRewardedId = admobRewardedId
        self.admobAppOpenId = admobAppOpenId
        self.appsFlyerKey = appsFlyerKey
        self.appsFlyerAppId = appsFlyerAppId
        self.paySubsIds = paySubsIds
        self.payConsumableIds = payConsumableIds
        self.payNonConsumableIds = payNonConsumableIds
        self.analyticsDebug = analyticsDebug
        self.remoteConfigMinFetchIntervalSeconds = remoteConfigMinFetchIntervalSeconds
        self.registerPushAtLaunch = registerPushAtLaunch
        self.deferCrashReport = deferCrashReport
        self.debug = debug
    }
}

private final class BootstrapState: @unchecked Sendable {
    let lock = NSLock()
    var delegatesInstalled = false
    var essentialInitialized = false
    var deferredInitialized = false
    var pendingConfig: IosKitBootstrapConfig?
}

/**
 * iOS 原生商业化与基础设施 Kit 引导器（NativeKitBootstrap）。
 *
 * 采用 **方案 B（双阶段分级初始化）**，与 Android 端 `NativeKitBootstrap` 保持 1:1 对称：
 * - **Stage 1 (`initEssentialKits`)**：冷启动仅幂等装配全量 9 大 Swift Delegate 并立即激活本地轻量必需 Kit；
 * - **Stage 2 (`initDeferredKits`)**：于 Compose 首帧完成或隐私合规弹窗同意后幂等激活三方/商业化/网络敏感 Kit。
 */
public final class NativeKitBootstrap: Sendable {
    private static let state = BootstrapState()

    private static func configureFirebaseIfAvailable() -> Bool {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else {
            return false
        }
        #if canImport(FirebaseCore)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        return FirebaseApp.app() != nil
        #else
        return false
        #endif
    }

    /**
     * Stage 1（冷启动阶段）：幂等装配全部 Swift Delegate，并仅激活纯本地轻量与基础必需 Kit。
     */
    public static func initEssentialKits(config: IosKitBootstrapConfig = IosKitBootstrapConfig()) {
        state.lock.lock()
        defer { state.lock.unlock() }

        state.pendingConfig = config
        if !state.delegatesInstalled {
            installDelegatesLocked(config: config)
            state.delegatesInstalled = true
        }
        if !state.essentialInitialized {
            initEssentialLocked(deferCrashReport: config.deferCrashReport)
            state.essentialInitialized = true
            // [KIT:log:START]
            CoreLog.shared.i(tag: LogTags.shared.APP, message: "[KMP-iOS] Stage 1 essential kits initialized.")
            // [KIT:log:END]
        }
    }

    /**
     * Stage 2（首帧完成 / 隐私合规同意后）：幂等激活商业化、归因、远程配置与推送等三方 Kit。
     */
    @discardableResult
    public static func initDeferredKits() -> Bool {
        state.lock.lock()
        defer { state.lock.unlock() }

        guard !state.deferredInitialized, state.delegatesInstalled, let config = state.pendingConfig else {
            return false
        }

        let firebaseConfigured = configureFirebaseIfAvailable()

        // [KIT:crashreport:START]
        if config.deferCrashReport {
            CoreCrashReport.shared.doInit()
        }
        // [KIT:crashreport:END]

        // [KIT:analytics:START]
        CoreAnalytics.shared.doInit()
        // [KIT:analytics:END]

        // [KIT:abtesting:START]
        CoreAbTesting.shared.doInit()
        // [KIT:abtesting:END]

        // [KIT:mmp:START]
        CoreMmp.shared.doInit(appToken: config.appsFlyerKey)
        // [KIT:mmp:END]

        // [KIT:ads:START]
        CoreAds.shared.doInit(apiKey: config.admobAppId)
        // [KIT:ads:END]

        // [KIT:pay:START]
        CorePay.shared.doInit(
            catalog: PayProductCatalog.companion.fromCsv(
                subsCsv: config.paySubsIds,
                consumableCsv: config.payConsumableIds,
                nonConsumableCsv: config.payNonConsumableIds
            )
        )
        // [KIT:pay:END]

        // [KIT:push:START]
        CorePush.shared.doInit(registerFcm: config.registerPushAtLaunch ?? firebaseConfigured)
        // [KIT:push:END]

        state.deferredInitialized = true
        // [KIT:log:START]
        CoreLog.shared.i(tag: LogTags.shared.APP, message: "[KMP-iOS] Stage 2 deferred kits initialized.")
        // [KIT:log:END]
        return true
    }

    // [KIT:push:START]
    /**
     * 透传 APNs 设备 Token 至 PushKit 并同步至 CorePush 门面（隔离 iOSApp 入口对 PushKit 的直接依赖）。
     */
    public static func handlePushDeviceToken(_ deviceToken: Data) {
        IosPushDelegate.handleDeviceToken(deviceToken)
    }
    // [KIT:push:END]

    /**
     * 兼容一站式入口：依次完成 Stage 1 基础装配激活与 Stage 2 延迟 Kit 激活（常用于单测或无需延迟场景）。
     */
    public static func initKits(
        admobAppId: String = "",
        admobBannerId: String = "",
        admobInterstitialId: String = "",
        admobRewardedId: String = "",
        admobAppOpenId: String = "",
        appsFlyerKey: String = "",
        appsFlyerAppId: String = "",
        paySubsIds: String = "",
        payConsumableIds: String = "",
        payNonConsumableIds: String = "",
        analyticsDebug: Bool? = nil,
        remoteConfigMinFetchIntervalSeconds: TimeInterval? = nil,
        registerPushAtLaunch: Bool? = nil,
        debug: Bool = true
    ) {
        let config = IosKitBootstrapConfig(
            admobAppId: admobAppId,
            admobBannerId: admobBannerId,
            admobInterstitialId: admobInterstitialId,
            admobRewardedId: admobRewardedId,
            admobAppOpenId: admobAppOpenId,
            appsFlyerKey: appsFlyerKey,
            appsFlyerAppId: appsFlyerAppId,
            paySubsIds: paySubsIds,
            payConsumableIds: payConsumableIds,
            payNonConsumableIds: payNonConsumableIds,
            analyticsDebug: analyticsDebug,
            remoteConfigMinFetchIntervalSeconds: remoteConfigMinFetchIntervalSeconds,
            registerPushAtLaunch: registerPushAtLaunch,
            debug: debug
        )
        initEssentialKits(config: config)
        _ = initDeferredKits()
    }

    private static func installDelegatesLocked(config: IosKitBootstrapConfig) {
        let effectiveAnalyticsDebug = config.analyticsDebug ?? config.debug

        // [KIT_INSTALL_START]
        // [KIT:log:START]
        CoreLog.shared.install(delegate: IosLogDelegate())
        // [KIT:log:END]

        // [KIT:crashreport:START]
        CoreCrashReport.shared.install(delegate: IosCrashReportDelegate())
        // [KIT:crashreport:END]

        // [KIT:analytics:START]
        CoreAnalytics.shared.install(delegate: IosAnalyticsDelegate(debug: effectiveAnalyticsDebug))
        // [KIT:analytics:END]

        // [KIT:abtesting:START]
        CoreAbTesting.shared.install(
            delegate: IosAbTestingDelegate(
                debug: config.debug,
                minimumFetchIntervalInSeconds: config.remoteConfigMinFetchIntervalSeconds
            )
        )
        // [KIT:abtesting:END]

        // [KIT:mmp:START]
        CoreMmp.shared.install(delegate: IosMmpDelegate(debug: config.debug, appId: config.appsFlyerAppId))
        // [KIT:mmp:END]

        // [KIT:ads:START]
        CoreAds.shared.install(
            delegate: IosAdsDelegate(
                defaultBannerUnitId: config.admobBannerId,
                defaultInterstitialUnitId: config.admobInterstitialId,
                defaultRewardedUnitId: config.admobRewardedId,
                defaultAppOpenUnitId: config.admobAppOpenId,
                debug: config.debug
            )
        )
        // [KIT:ads:END]

        // [KIT:pay:START]
        CorePay.shared.install(delegate: IosPayDelegate())
        // [KIT:pay:END]

        // [KIT:push:START]
        CorePush.shared.install(delegate: IosPushDelegate())
        // [KIT:push:END]

        // [KIT:video:START]
        CoreVideo.shared.install(delegate: IosVideoDelegate())
        // [KIT:video:END]
        // [KIT_INSTALL_END]
    }

    private static func initEssentialLocked(deferCrashReport: Bool) {
        // [KIT:log:START]
        CoreLog.shared.doInitAllLog()
        // [KIT:log:END]

        // [KIT:crashreport:START]
        IosCrashHook.shared.doInstall()
        if !deferCrashReport {
            _ = configureFirebaseIfAvailable()
            CoreCrashReport.shared.doInit()
        }
        // [KIT:crashreport:END]

        // [KIT:video:START]
        CoreVideo.shared.doInit()
        // [KIT:video:END]

        // [KIT:pag:START]
        CorePag.shared.doInit()
        // [KIT:pag:END]
    }
}
