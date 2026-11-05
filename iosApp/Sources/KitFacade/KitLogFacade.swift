import Foundation
import LogKit

/**
 * LogKit 跨平台 Objective-C 薄门面。
 * 供 Kotlin/Native 通过 cinterop 直接调用，将 Kotlin 的日志调用 1:1 透传给纯 Swift 的 LogKit-iOS。
 */
@objc(KitLogFacade)
public final class KitLogFacade: NSObject {
    /**
     * 初始化底层日志系统。
     *
     * @param withDisk 是否开启磁盘日志写入
     */
    @objc public static func initializeWithDisk(_ withDisk: Bool) {
        if withDisk {
            LogKit.initAllLog()
        } else {
            LogKit.initOnlyConsoleLog()
        }
    }

    @objc public static func d(_ tag: String, message: String) {
        LogKit.d(tag, message)
    }

    @objc public static func i(_ tag: String, message: String) {
        LogKit.i(tag, message)
    }

    @objc public static func w(_ tag: String, message: String) {
        LogKit.w(tag, message)
    }

    @objc public static func e(_ tag: String, message: String, errorDescription: String?) {
        if let errorDescription = errorDescription {
            let error = NSError(domain: "KitLog", code: -1, userInfo: [NSLocalizedDescriptionKey: errorDescription])
            LogKit.e(tag, error: error, message)
        } else {
            LogKit.e(tag, message)
        }
    }
}
