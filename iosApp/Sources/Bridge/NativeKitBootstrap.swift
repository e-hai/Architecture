import Foundation
import SwiftUI

#if canImport(FirebaseCore)
import FirebaseCore
#endif

#if !canImport(Shared)
// [KIT_FALLBACK_IMPORTS_START]
// [KIT:abtesting:START]
import AbTesting
#if canImport(AbTestingFirebase)
import AbTestingFirebase
#endif
// [KIT:abtesting:END]
// [KIT:ads:START]
import AdsKit
// [KIT:ads:END]
// [KIT:analytics:START]
import Analytics
#if canImport(AnalyticsFirebase)
import AnalyticsFirebase
#endif
// [KIT:analytics:END]
// [KIT:crashreport:START]
import CrashReportKit
// [KIT:crashreport:END]
// [KIT:log:START]
import LogKit
// [KIT:log:END]
// [KIT:mmp:START]
import MmpCore
#if canImport(MmpAppsFlyer)
import MmpAppsFlyer
#endif
// [KIT:mmp:END]
// [KIT:pay:START]
import PayKit
// [KIT:pay:END]
// [KIT:push:START]
import PushKit
// [KIT:push:END]
// [KIT:video:START]
import VideoKit
// [KIT:video:END]
// [KIT_FALLBACK_IMPORTS_END]
#endif

#if canImport(Shared)
import Shared
#endif

/**
 * iOS 原生商业化与基础设施 Kit 引导器（NativeKitBootstrap）。
 * 专职负责十大 Kit 的 Swift 委托装配与跨平台门面挂载（委托实现均独立拆分在 Delegates 目录），
 * 与 Android 端的 NativeKitBootstrap 保持 1:1 对称与相同设计范式。
 */
public final class NativeKitBootstrap: Sendable {
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
     * 批量初始化并注册所有 iOS 原生 Kit 的 Swift SPM 委托实现至 Core 跨平台门面。
     */
    public static func initKits(
        admobAppId: String = "",
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
        print("[KMP-iOS] Initializing native Kit SPM SDKs & Delegates...")
        let firebaseConfigured = configureFirebaseIfAvailable()
        let effectiveAnalyticsDebug = analyticsDebug ?? debug

        #if canImport(Shared)
        // [KIT_INSTALL_START]
        // [KIT:log:START]
        CoreLog.shared.install(delegate: IosLogDelegate())
        CoreLog.shared.doInitAllLog()
        // [KIT:log:END]

        // [KIT:crashreport:START]
        CoreCrashReport.shared.install(delegate: IosCrashReportDelegate())
        CoreCrashReport.shared.doInit()
        // [KIT:crashreport:END]

        // [KIT:analytics:START]
        CoreAnalytics.shared.install(delegate: IosAnalyticsDelegate(debug: effectiveAnalyticsDebug))
        CoreAnalytics.shared.doInit()
        // [KIT:analytics:END]

        // [KIT:abtesting:START]
        CoreAbTesting.shared.install(
            delegate: IosAbTestingDelegate(
                debug: debug,
                minimumFetchIntervalInSeconds: remoteConfigMinFetchIntervalSeconds
            )
        )
        CoreAbTesting.shared.doInit()
        // [KIT:abtesting:END]

        // [KIT:mmp:START]
        CoreMmp.shared.install(delegate: IosMmpDelegate(debug: debug, appId: appsFlyerAppId))
        CoreMmp.shared.doInit(appToken: appsFlyerKey)
        // [KIT:mmp:END]

        // [KIT:ads:START]
        CoreAds.shared.install(delegate: IosAdsDelegate(debug: debug))
        CoreAds.shared.doInit(apiKey: admobAppId)
        // [KIT:ads:END]

        // [KIT:pay:START]
        CorePay.shared.install(delegate: IosPayDelegate())
        CorePay.shared.doInit(
            catalog: PayProductCatalog.companion.fromCsv(
                subsCsv: paySubsIds,
                consumableCsv: payConsumableIds,
                nonConsumableCsv: payNonConsumableIds
            )
        )
        // [KIT:pay:END]

        // [KIT:push:START]
        CorePush.shared.install(delegate: IosPushDelegate())
        CorePush.shared.doInit(registerFcm: registerPushAtLaunch ?? firebaseConfigured)
        // [KIT:push:END]

        // [KIT:video:START]
        CoreVideo.shared.install(delegate: IosVideoDelegate())
        CoreVideo.shared.doInit()
        // [KIT:video:END]

        // [KIT:pag:START]
        CorePag.shared.doInit()
        // [KIT:pag:END]
        // [KIT_INSTALL_END]

        print("[KMP-iOS] Native Kit delegates successfully installed and initialized.")
        #else
        // CLI 测试环境兜底（当未链接 Shared.framework 时）
        // [KIT_FALLBACK_INIT_START]
        // [KIT:log:START]
        LogKit.initAllLog()
        // [KIT:log:END]
        // [KIT:crashreport:START]
        CrashReport.initialize(config: CrashReportConfig(enabled: true))
        // [KIT:crashreport:END]
        // [KIT:analytics:START]
        var analyticsProviders: [AnalyticsProvider] = []
        if effectiveAnalyticsDebug {
            analyticsProviders.append(LoggingAnalyticsProvider())
        }
        #if canImport(AnalyticsFirebase)
        if firebaseConfigured {
            analyticsProviders.append(FirebaseAnalyticsProvider())
        }
        #endif
        Analytics.initialize(config: AnalyticsConfig(
            providers: analyticsProviders,
            enabled: true,
            debug: effectiveAnalyticsDebug
        ))
        // [KIT:analytics:END]
        // [KIT:abtesting:START]
        #if canImport(AbTestingFirebase)
        if firebaseConfigured {
            AbTestingClient.install(
                FirebaseAbTestingProvider(
                    config: AbTestingConfig(minimumFetchIntervalInSeconds: remoteConfigMinFetchIntervalSeconds ?? (debug ? 0 : 3600))
                )
            )
        } else {
            AbTestingClient.install(FakeAbTestingProvider())
        }
        #else
        AbTestingClient.install(FakeAbTestingProvider())
        #endif
        // [KIT:abtesting:END]
        // [KIT:mmp:START]
        let trimmedMmpToken = appsFlyerKey.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmedMmpToken.isEmpty {
            #if canImport(MmpAppsFlyer)
            Mmp.initialize(
                config: MmpConfig(appToken: trimmedMmpToken, isDebug: debug, appId: appsFlyerAppId, enableLog: debug),
                client: AppsFlyerClient()
            )
            #else
            Mmp.setLogEnabled(debug)
            #endif
        } else {
            Mmp.setLogEnabled(debug)
        }
        // [KIT:mmp:END]
        // [KIT:pay:START]
        PayKit.initialize(configuration: PayKitConfiguration())
        // [KIT:pay:END]
        // [KIT:push:START]
        if Bundle.main.bundleIdentifier != nil {
            PushKitManager.shared.setup()
        }
        // [KIT:push:END]
        // [KIT:video:START]
        _ = VideoCacheManager.shared
        // [KIT:video:END]
        // [KIT_FALLBACK_INIT_END]
        #endif
    }
}
