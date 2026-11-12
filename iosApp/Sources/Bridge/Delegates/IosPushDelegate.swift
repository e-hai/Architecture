import Foundation
import PushKit

#if canImport(Shared)
import Shared

/**
 * iOS 推送通知委托实现（1:1 透传 PushKit-iOS 原生 SDK）。
 */
internal final class IosPushDelegate: NSObject, PushDelegate {
    func doInit(registerFcm: Bool) {
        if Bundle.main.bundleIdentifier != nil {
            PushKitManager.shared.setup()
        }
    }

    func registerPush() {
        Task {
            _ = try? await PushKitManager.shared.requestPermission()
        }
    }

    func setPushToken(token: String) {
        print("[IosPushDelegate] setPushToken: \(token)")
    }
}
#endif
