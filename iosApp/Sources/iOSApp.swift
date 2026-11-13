// [KIT:push:START]
import PushKit
// [KIT:push:END]
import SwiftUI
#if canImport(UIKit)
import UIKit
#endif

#if canImport(Shared)
import Shared

// [KIT:push:START]
/**
 * iOS UIApplicationDelegate 适配器，负责接收 APNs 设备 Token 与远程推送回调并回传至 CorePush / PushKit。
 */
public final class AppDelegate: NSObject, UIApplicationDelegate {
    public func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        PushKitManager.shared.apnsProvider.handleDeviceToken(deviceToken)
        if let token = PushKitManager.shared.apnsProvider.getToken(), !token.isEmpty {
            CorePush.shared.setPushToken(token: token)
        }
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
 *
 * 注：仅在存在 Shared.framework 的完整应用工程中编译生效。
 */
@main
public struct iOSApp: App {
    // [KIT:push:START]
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    // [KIT:push:END]

    public init() {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif

        let info = Bundle.main.infoDictionary
        let admobAppId = info?["GADApplicationIdentifier"] as? String ?? ""
        let baseUrl = info?["BASE_URL"] as? String ?? ""
        let appsFlyerKey = info?["APPSFLYER_KEY"] as? String ?? ""
        let appsFlyerAppId = info?["APPSFLYER_APP_ID"] as? String ?? ""
        let paySubsIds = info?["PAY_SUBS_IDS"] as? String ?? ""
        let payConsumableIds = info?["PAY_CONSUMABLE_IDS"] as? String ?? ""
        let payNonConsumableIds = info?["PAY_NON_CONSUMABLE_IDS"] as? String ?? ""
        let analyticsDebugRaw = info?["ANALYTICS_DEBUG_MODE"] as? String ?? ""
        let analyticsDebug = analyticsDebugRaw.isEmpty
            ? isDebug
            : (analyticsDebugRaw.caseInsensitiveCompare("YES") == .orderedSame || analyticsDebugRaw == "true")
        let fetchIntervalRaw = info?["REMOTE_CONFIG_MIN_FETCH_INTERVAL_SECONDS"] as? String ?? ""
        let remoteConfigMinFetchInterval = Double(fetchIntervalRaw)

        // 1. 初始化十大基础设施与商业化 Kit (9 大 Swift SPM 委托 + PagKit)
        NativeKitBootstrap.initKits(
            admobAppId: admobAppId,
            appsFlyerKey: appsFlyerKey,
            appsFlyerAppId: appsFlyerAppId,
            paySubsIds: paySubsIds,
            payConsumableIds: payConsumableIds,
            payNonConsumableIds: payNonConsumableIds,
            analyticsDebug: analyticsDebug,
            remoteConfigMinFetchIntervalSeconds: remoteConfigMinFetchInterval,
            debug: isDebug
        )

        // 2. 初始化已实现跨平台的纯 KMP 库 (Koin: Room / DataStore / Ktor) 并注入宿主 AppConfig
        let config = AppConfig(
            env: isDebug ? .dev : .prod,
            isDebug: isDebug,
            baseUrl: baseUrl,
            versionName: info?["CFBundleShortVersionString"] as? String ?? "1.0.0",
            versionCode: Int32(info?["CFBundleVersion"] as? String ?? "1") ?? 1
        )
        AppBootstrap.shared.doInitKoin(appConfig: config)
    }

    public var body: some Scene {
        WindowGroup {
            ContentView()
                // [KIT:mmp:START]
                .onOpenURL { url in
                    IosMmpBridge.handleOpenUrl(url)
                }
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { userActivity in
                    IosMmpBridge.continueUserActivity(userActivity)
                }
                // [KIT:mmp:END]
        }
    }
}
#endif
