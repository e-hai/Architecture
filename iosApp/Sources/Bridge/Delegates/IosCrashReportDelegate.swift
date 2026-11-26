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

    // Kotlin/Native ObjC 导出为避免方法签名冲突生成 userId_ 参数名，必须保留以匹配 CrashReportDelegate 协议
    // swiftlint:disable:next identifier_name
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
        let exceptionType = IosCrashHook.shared.exceptionTypeName(throwable: throwable)
        let stackTrace = IosCrashHook.shared.formatStackTrace(throwable: throwable)
        CrashReport.setCustomKey("kotlin_exception_type", value: exceptionType)
        CrashReport.log("[KotlinException] \(stackTrace)")
        let error = NSError(
            domain: exceptionType,
            code: -1,
            userInfo: [
                NSLocalizedDescriptionKey: throwable.message ?? exceptionType,
                NSLocalizedFailureReasonErrorKey: stackTrace
            ]
        )
        CrashReport.recordException(error)
    }
}
