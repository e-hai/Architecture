import Foundation

/// HTTP 请求方法枚举
public enum HTTPMethod: String, Sendable {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
    case patch = "PATCH"
}

/// 网络请求端点描述协议。
public protocol Endpoint: Sendable {
    var baseURL: URL { get }
    var path: String { get }
    var method: HTTPMethod { get }
    var headers: [String: String]? { get }
    var queryItems: [URLQueryItem]? { get }
    var body: Data? { get }
}

public extension Endpoint {
    var headers: [String: String]? { nil }
    var queryItems: [URLQueryItem]? { nil }
    var body: Data? { nil }

    /// 生成完整的 URLRequest
    func asURLRequest() throws -> URLRequest {
        var url = baseURL.appendingPathComponent(path)
        if let queryItems = queryItems, !queryItems.isEmpty {
            guard var components = URLComponents(url: url, resolvingAgainstBaseURL: false) else {
                throw URLError(.badURL)
            }
            components.queryItems = queryItems
            if let resolvedURL = components.url {
                url = resolvedURL
            }
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.allHTTPHeaderFields = headers
        request.httpBody = body
        return request
    }
}
