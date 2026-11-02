import Foundation

/// 跨模块通用应用错误类型。
public enum AppError: Error, LocalizedError, Sendable, Hashable {
    case network(String)
    case notFound
    case unauthorized
    case parseError(String)
    case storageError(String)
    case business(code: Int, message: String)
    case unknown(String)

    public var errorDescription: String? {
        switch self {
        case .network(let message):
            return "网络异常: \(message)"
        case .notFound:
            return "请求的资源未找到"
        case .unauthorized:
            return "登录状态已失效，请重新登录"
        case .parseError(let detail):
            return "数据解析失败: \(detail)"
        case .storageError(let detail):
            return "本地存储异常: \(detail)"
        case .business(_, let message):
            return message
        case .unknown(let detail):
            return "未知错误: \(detail)"
        }
    }
}
