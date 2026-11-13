import CrashReportKit
import Foundation
import Shared

/**
 * iOS 崩溃采集委托实现（1:1 透传 CrashReportKit-iOS 原生 SDK）。
 */
internal final class IosCrashReportDelegate: NSObject, CrashReportDelegate {
    func doInit() {
        CrashReport.initialize(config: CrashReportConfig(enabled: true))
    }

    func setUserId(userId_: String) {
        CrashReport.setUserId(userId_)
    }

    func setCustomKey(key: String, value: Any) {
        CrashReport.setCustomKey(key, value: String(describing: value))
    }

    func log(message: String) {
        CrashReport.log(message)
    }

    func recordException(throwable: KotlinThrowable) {
        let error = NSError(
            domain: "KotlinThrowable",
            code: -1,
            userInfo: [NSLocalizedDescriptionKey: throwable.message ?? "Kotlin Exception"]
        )
        CrashReport.recordException(error)
    }
}
