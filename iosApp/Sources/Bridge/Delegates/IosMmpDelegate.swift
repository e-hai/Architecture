import Foundation
import MmpCore

#if canImport(Shared)
import Shared

/**
 * iOS 归因统计委托实现（1:1 透传 MmpKit-iOS 原生 SDK）。
 */
internal final class IosMmpDelegate: NSObject, MmpDelegate {
    func doInit(appToken: String) {
        Mmp.setLogEnabled(true)
    }

    func trackEvent(eventName: String, params: [String: Any]) {
        Mmp.trackEvent(eventName, params: params)
    }

    func setCustomerUserId(userId: String?) {
        if let uid = userId {
            Mmp.trackEvent("set_customer_user_id", params: ["user_id": uid])
        }
    }
}
#endif
