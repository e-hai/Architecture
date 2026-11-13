import AbTesting
import Foundation

#if canImport(AbTestingFirebase)
import AbTestingFirebase
#endif

#if canImport(Shared)
import Shared

/**
 * iOS A/B 测试委托实现（优先挂载 Firebase Remote Config，缺失 GoogleService-Info.plist 时安全降级本地默认值）。
 */
internal final class IosAbTestingDelegate: NSObject, AbTestingDelegate, @unchecked Sendable {
    private let debug: Bool
    private let minimumFetchIntervalInSeconds: TimeInterval

    init(
        debug: Bool = true,
        minimumFetchIntervalInSeconds: TimeInterval? = nil
    ) {
        self.debug = debug
        self.minimumFetchIntervalInSeconds = minimumFetchIntervalInSeconds ?? (debug ? 0 : 3600)
        super.init()
    }

    func doInit() {
        let defaults = AbTestingKeys.shared.defaults()

        #if canImport(AbTestingFirebase)
        if isFirebaseInitialized() {
            let provider = FirebaseAbTestingProvider(
                config: AbTestingConfig(minimumFetchIntervalInSeconds: minimumFetchIntervalInSeconds)
            )
            AbTestingClient.install(provider)
            Task {
                do {
                    try await AbTestingClient.setDefaults(defaults)
                    let activated = try await AbTestingClient.fetchAndActivate()
                    CoreLog.shared.d(
                        tag: LogTags.shared.AB_TESTING,
                        message: "[AbTesting-iOS] Firebase fetchAndActivate=\(activated)"
                    )
                } catch {
                    CoreLog.shared.w(
                        tag: LogTags.shared.AB_TESTING,
                        message: "[AbTesting-iOS] Firebase fetchAndActivate failed: \(error.localizedDescription)"
                    )
                }
            }
            return
        }
        #endif

        let fallback = FakeAbTestingProvider()
        AbTestingClient.install(fallback)
        Task {
            try? await AbTestingClient.setDefaults(defaults)
        }
        CoreLog.shared.w(
            tag: LogTags.shared.AB_TESTING,
            message: "[AbTesting-iOS] Firebase 未初始化 (缺少 GoogleService-Info.plist)，已降级本地默认值模式"
        )
    }

    func getBoolean(key: String, defaultValue: Bool) -> Bool {
        AbTestingClient.getBoolean(key, default: defaultValue)
    }

    func getString(key: String, defaultValue: String) -> String {
        AbTestingClient.getString(key, default: defaultValue)
    }

    func getLong(key: String, defaultValue: Int64) -> Int64 {
        AbTestingClient.getLong(key, default: defaultValue)
    }

    func getDouble(key: String, defaultValue: Double) -> Double {
        AbTestingClient.getDouble(key, default: defaultValue)
    }
}
#endif
