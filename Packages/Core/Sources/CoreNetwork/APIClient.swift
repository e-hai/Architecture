import Foundation
import CoreLog

/// 网络客户端协议。
public protocol APIClientProtocol: Sendable {
    /// 执行网络请求并解码为泛型模型
    func request<T: Decodable & Sendable>(_ endpoint: Endpoint) async throws -> T
    /// 执行无返回值网络请求
    func requestRaw(_ endpoint: Endpoint) async throws -> Data
}

/// 默认 APIClient 实现类。
public final class APIClient: APIClientProtocol {
    private let session: URLSession
    private let decoder: JSONDecoder

    public init(
        session: URLSession = .shared,
        decoder: JSONDecoder = {
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            return decoder
        }()
    ) {
        self.session = session
        self.decoder = decoder
    }

    public func request<T: Decodable & Sendable>(_ endpoint: Endpoint) async throws -> T {
        let data = try await requestRaw(endpoint)
        do {
            return try decoder.decode(T.self, from: data)
        } catch {
            LogKit.e("JSON 解码失败: \(error)", tag: .network)
            throw NetworkError.decodingError(error)
        }
    }

    public func requestRaw(_ endpoint: Endpoint) async throws -> Data {
        let request: URLRequest
        do {
            request = try endpoint.asURLRequest()
        } catch {
            throw NetworkError.invalidURL
        }

        LogKit.d("发起请求: [\(request.httpMethod ?? "GET")] \(request.url?.absoluteString ?? "")", tag: .network)

        let (data, response): (Data, URLResponse)
        do {
            (data, response) = try await session.data(for: request)
        } catch {
            LogKit.e("网络连接失败", error: error, tag: .network)
            throw NetworkError.requestFailed(error)
        }

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse(statusCode: -1)
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            LogKit.w("HTTP 异常状态码: \(httpResponse.statusCode)", tag: .network)
            if httpResponse.statusCode == 401 {
                throw NetworkError.unauthorized
            }
            throw NetworkError.invalidResponse(statusCode: httpResponse.statusCode)
        }

        return data
    }
}
