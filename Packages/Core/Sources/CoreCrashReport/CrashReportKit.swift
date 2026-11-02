import Foundation
import CoreLog

/// 崩溃日志记录与上报门面。
public enum CrashReport {
    /// 记录非致命异常
    public static func recordError(_ error: Error, additionalInfo: [String: Any]? = nil) {
        LogKit.e("CrashReport 记录异常: \(error.localizedDescription) 附加参数: \(additionalInfo ?? [:])", tag: .app)
    }

    /// 设置自定义维度 Key
    public static func setCustomValue(_ value: Any, forKey key: String) {
        LogKit.d("CrashReport 设置自定义属性 [\(key)] = \(value)", tag: .app)
    }

    /// 记录关键面包屑路径日志
    public static func log(_ message: String) {
        LogKit.d("CrashReport 面包屑日志: \(message)", tag: .app)
    }
}
