import Foundation

/// 网络层专属错误定义。
public enum NetworkError: Error, LocalizedError, Sendable {
    case invalidURL
    case requestFailed(Error)
    case invalidResponse(statusCode: Int)
    case decodingError(Error)
    case unauthorized
    case noData

    public var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "URL 格式无效"
        case .requestFailed(let error):
            return "网络连接失败: \(error.localizedDescription)"
        case .invalidResponse(let statusCode):
            return "服务器响应异常状态码: \(statusCode)"
        case .decodingError(let error):
            return "数据解码失败: \(error.localizedDescription)"
        case .unauthorized:
            return "鉴权失败或未登录"
        case .noData:
            return "服务器未返回数据"
        }
    }
}
