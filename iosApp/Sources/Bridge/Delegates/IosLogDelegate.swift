import Foundation
import LogKit

#if canImport(Shared)
import Shared
import UIKit

/**
 * iOS 日志委托实现（1:1 透传 LogKit-iOS 原生 SDK）。
 */
internal final class IosLogDelegate: NSObject, LogDelegate {
    func doInitAllLog() {
        LogKit.initAllLog()
    }

    func doInitOnlyConsoleLog() {
        LogKit.initOnlyConsoleLog()
    }

    func doInitOnlyDiskLog() {
        LogKit.initOnlyDiskLog()
    }

    func v(tag: String, message: String) {
        LogKit.v(tag, message)
    }

    func d(tag: String, message: String) {
        LogKit.d(tag, message)
    }

    func i(tag: String, message: String) {
        LogKit.i(tag, message)
    }

    func w(tag: String, message: String) {
        LogKit.w(tag, message)
    }

    func e(tag: String, message: String, throwable: KotlinThrowable?) {
        if let err = throwable?.message {
            LogKit.e(tag, "\(message) | \(err)")
        } else {
            LogKit.e(tag, message)
        }
    }

    func wtf(tag: String, message: String) {
        LogKit.wtf(tag, message)
    }

    func json(tag: String, json: String) {
        LogKit.json(tag, json)
    }

    func xml(tag: String, xml: String) {
        LogKit.xml(tag, xml)
    }

    func readLog(callback: @escaping ([String]) -> Void) {
        LogKit.readLog { lines in
            callback(lines)
        }
    }

    func showLogUi() {
        DispatchQueue.main.async {
            guard let topVC = self.getTopViewController() else {
                LogKit.w("App", "No active UIViewController found to present LogKit UI")
                return
            }
            LogKit.showLogUI(from: topVC)
        }
    }

    @MainActor
    private func getTopViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }) ?? UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene }).first,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }) ?? windowScene.windows.first,
              var topController = window.rootViewController else {
            return nil
        }
        while let presented = topController.presentedViewController {
            topController = presented
        }
        return topController
    }
}
#endif
