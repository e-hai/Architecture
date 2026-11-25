import Shared
import SwiftUI
import UIKit

// [KIT:push:START]
/**
 * iOS UIApplicationDelegate 适配器，负责接收 APNs 设备 Token 与远程推送回调并回传至 CorePush / PushKit。
 */
public final class AppDelegate: NSObject, UIApplicationDelegate {
    public func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        NativeKitBootstrap.handlePushDeviceToken(deviceToken)
    }

    public func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        CoreLog.shared.w(
            tag: LogTags.shared.PUSH,
            message: "[Push-iOS] APNs 注册失败: \(error.localizedDescription)"
        )
    }
}
// [KIT:push:END]

/**
 * iOS 独立应用程序系统入口（遵循 SwiftUI App 协议）。
 *
 * 职责：
 * 1. 作为完整 iOS 可执行 App（.app）的独立主入口（标记 @main）；
 * 2. 负责顶层 WindowGroup 窗口生命周期与渲染容器配置；
 * 3. 执行冷启动标准化初始化：
 *    - Phase 1: NativeKitBootstrap.initKits()（未跨平台原生 Kit 初始化与桥接）
 *    - Phase 2: AppBootstrap.shared.doInitKoin()（已跨平台纯 KMP 库 Koin 容器启动）
 */
@main
public struct iOSApp: App {
    // [KIT:push:START]
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    // [KIT:push:END]

    public init() {
        #if DEBUG
        let isDebug = true
        let appEnv: AppEnvironment = .dev
        #else
        let isDebug = false
        let appEnv: AppEnvironment = .prod
        #endif

        let info = Bundle.main.infoDictionary
        let admobAppId = info?["GADApplicationIdentifier"] as? String ?? ""
        let admobBannerId = info?["ADMOB_BANNER_ID"] as? String ?? ""
        let admobInterstitialId = info?["ADMOB_INTERSTITIAL_ID"] as? String ?? ""
        let admobRewardedId = info?["ADMOB_REWARDED_ID"] as? String ?? ""
        let admobAppOpenId = info?["ADMOB_APP_OPEN_ID"] as? String ?? ""
        let baseUrl = Self.resolveBaseUrl(from: info)
        let appsFlyerKey = info?["APPSFLYER_KEY"] as? String ?? ""
        let appsFlyerAppId = info?["APPSFLYER_APP_ID"] as? String ?? ""
        let paySubsIds = info?["PAY_SUBS_IDS"] as? String ?? ""
        let payConsumableIds = info?["PAY_CONSUMABLE_IDS"] as? String ?? ""
        let payNonConsumableIds = info?["PAY_NON_CONSUMABLE_IDS"] as? String ?? ""
        let analyticsDebugRaw = info?["ANALYTICS_DEBUG_MODE"] as? String ?? ""
        let analyticsDebug = analyticsDebugRaw.isEmpty
            ? isDebug
            : (analyticsDebugRaw.caseInsensitiveCompare("YES") == .orderedSame || analyticsDebugRaw == "true")
        let httpLogRaw = info?["ENABLE_HTTP_LOG"] as? String ?? ""
        let enableHttpLog = httpLogRaw.isEmpty
            ? isDebug
            : (httpLogRaw.caseInsensitiveCompare("YES") == .orderedSame || httpLogRaw == "true")
        let fetchIntervalRaw = info?["REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS"] as? String ?? ""
        let remoteConfigMinFetchInterval = Double(fetchIntervalRaw)

        // 1. Stage 1: 冷启动全量装配 9 大 Swift Delegate + 仅激活本地轻量必需 Kit
        let bootstrapConfig = IosKitBootstrapConfig(
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
            remoteConfigMinFetchIntervalSeconds: remoteConfigMinFetchInterval,
            debug: isDebug
        )
        NativeKitBootstrap.initEssentialKits(config: bootstrapConfig)

        // 2. 初始化已实现跨平台的纯 KMP 库 (Koin: Room / DataStore / Ktor) 并绑定 Stage 2 延迟初始化器
        let config = AppConfig(
            env: appEnv,
            isDebug: isDebug,
            enableHttpLog: enableHttpLog,
            baseUrl: baseUrl,
            versionName: info?["CFBundleShortVersionString"] as? String ?? "1.0.0",
            versionCode: Int32(info?["CFBundleVersion"] as? String ?? "1") ?? 1
        )
        AppBootstrap.shared.doInitKoin(appConfig: config)
        AppBootstrap.shared.registerDeferredKitInitializer {
            _ = NativeKitBootstrap.initDeferredKits()
        }
    }

    private static func resolveBaseUrl(from info: [String: Any]?) -> String {
        let rawHost = (info?["API_HOST"] as? String ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        if rawHost.isEmpty {
            return ""
        }
        if rawHost.hasPrefix("http://") || rawHost.hasPrefix("https://") {
            return rawHost
        }
        let rawScheme = (info?["API_SCHEME"] as? String ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        let scheme = rawScheme.isEmpty ? "https" : rawScheme
        return "\(scheme)://\(rawHost)"
    }

    public var body: some Scene {
        WindowGroup {
            ContentView()
                // [KIT:mmp:START]
                .onOpenURL { url in
                    _ = AppBootstrap.shared.doInitDeferredKits()
                    IosMmpBridge.handleOpenUrl(url)
                }
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { userActivity in
                    _ = AppBootstrap.shared.doInitDeferredKits()
                    IosMmpBridge.continueUserActivity(userActivity)
                }
                // [KIT:mmp:END]
        }
    }
}
