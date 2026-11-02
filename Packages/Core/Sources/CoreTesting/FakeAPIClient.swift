import Foundation
import CoreModel
import CoreNetwork

/// 单元测试专用 Fake 网络客户端。
public final class FakeAPIClient: APIClientProtocol, @unchecked Sendable {
    public var mockResult: Result<Data, Error>?

    public init(mockResult: Result<Data, Error>? = nil) {
        self.mockResult = mockResult
    }

    public func request<T: Decodable & Sendable>(_ endpoint: Endpoint) async throws -> T {
        let data = try await requestRaw(endpoint)
        return try JSONDecoder().decode(T.self, from: data)
    }

    public func requestRaw(_ endpoint: Endpoint) async throws -> Data {
        guard let mockResult = mockResult else {
            throw NetworkError.noData
        }
        switch mockResult {
        case .success(let data):
            return data
        case .failure(let error):
            throw error
        }
    }
}
