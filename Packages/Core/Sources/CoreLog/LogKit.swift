import Foundation
import os

/// 统一结构化日志门面。
public enum LogKit {
    private static let subsystem = "com.architecture.app"

    /// 跨模块通用 Tag
    public enum Tag: String, Sendable {
        case app = "App"
        case network = "Network"
        case storage = "Storage"
        case navigation = "Navigation"
        case analytics = "Analytics"
        case ads = "Ads"
        case pay = "Pay"
        case push = "Push"
        case ui = "UI"
    }

    nonisolated(unsafe) private static var loggers: [String: Logger] = [:]
    private static let lock = NSLock()

    private static func logger(for category: String) -> Logger {
        lock.lock()
        defer { lock.unlock() }
        if let existing = loggers[category] {
            return existing
        }
        let created = Logger(subsystem: subsystem, category: category)
        loggers[category] = created
        return created
    }

    /// Debug 级别日志
    public static func d(_ message: String, tag: Tag = .app) {
        #if DEBUG
        logger(for: tag.rawValue).debug("🔍 [\(tag.rawValue)] \(message, privacy: .public)")
        #endif
    }

    /// Info 级别日志
    public static func i(_ message: String, tag: Tag = .app) {
        logger(for: tag.rawValue).info("ℹ️ [\(tag.rawValue)] \(message, privacy: .public)")
    }

    /// Warning 级别日志
    public static func w(_ message: String, tag: Tag = .app) {
        logger(for: tag.rawValue).warning("⚠️ [\(tag.rawValue)] \(message, privacy: .public)")
    }

    /// Error 级别日志
    public static func e(_ message: String, error: Error? = nil, tag: Tag = .app) {
        if let error = error {
            logger(for: tag.rawValue).error("❌ [\(tag.rawValue)] \(message, privacy: .public) | error: \(error.localizedDescription, privacy: .public)")
        } else {
            logger(for: tag.rawValue).error("❌ [\(tag.rawValue)] \(message, privacy: .public)")
        }
    }
}
