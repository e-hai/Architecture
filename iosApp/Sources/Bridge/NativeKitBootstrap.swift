import Foundation
import SwiftUI

#if !canImport(Shared)
// [KIT_FALLBACK_IMPORTS_START]
// [KIT:abtesting:START]
import AbTesting
// [KIT:abtesting:END]
// [KIT:ads:START]
import AdsKit
// [KIT:ads:END]
// [KIT:analytics:START]
import Analytics
// [KIT:analytics:END]
// [KIT:crashreport:START]
import CrashReportKit
// [KIT:crashreport:END]
// [KIT:log:START]
import LogKit
// [KIT:log:END]
// [KIT:mmp:START]
import MmpCore
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
        paySubsIds: String = "",
        payConsumableIds: String = "",
        payNonConsumableIds: String = "",
        debug: Bool = true
    ) {
        print("[KMP-iOS] Initializing native Kit SPM SDKs & Delegates...")

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
        CoreAnalytics.shared.install(delegate: IosAnalyticsDelegate())
        CoreAnalytics.shared.doInit()
        // [KIT:analytics:END]

        // [KIT:abtesting:START]
        CoreAbTesting.shared.install(delegate: IosAbTestingDelegate())
        CoreAbTesting.shared.doInit()
        // [KIT:abtesting:END]

        // [KIT:mmp:START]
        CoreMmp.shared.install(delegate: IosMmpDelegate())
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
        CorePush.shared.doInit(registerFcm: false)
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
        Analytics.initialize(config: AnalyticsConfig(
            providers: [LoggingAnalyticsProvider()],
            enabled: true,
            debug: true
        ))
        // [KIT:analytics:END]
        // [KIT:abtesting:START]
        AbTestingClient.install(FakeAbTestingProvider())
        // [KIT:abtesting:END]
        // [KIT:mmp:START]
        Mmp.setLogEnabled(true)
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
