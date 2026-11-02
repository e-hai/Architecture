import Testing
import Foundation
@testable import CoreModel
@testable import CoreDI
@testable import CoreNavigation
@testable import CoreNetwork
@testable import CoreStorage
@testable import CoreTesting

@Suite("Core 基础模块功能测试套件")
struct CoreTests {

    @Test("验证 User 领域模型构建与序列化")
    func testUserModel() throws {
        let user = User(id: "u_1", nickname: "Alice", bio: "Hello World")
        #expect(user.id == "u_1")
        #expect(user.nickname == "Alice")

        let data = try JSONEncoder().encode(user)
        let decoded = try JSONDecoder().decode(User.self, from: data)
        #expect(decoded == user)
    }

    @Test("验证 CoreDI 容器依赖注册与解析")
    func testDependencyInjection() {
        let container = DependencyContainer()
        protocol ServiceProtocol: Sendable {
            func greet() -> String
        }
        final class ServiceImpl: ServiceProtocol {
            func greet() -> String { "Hello from Service" }
        }

        container.register(ServiceProtocol.self, scope: .singleton) {
            ServiceImpl()
        }

        let resolved = container.resolve(ServiceProtocol.self)
        #expect(resolved.greet() == "Hello from Service")
    }

    @Test("验证 FakeStorage 内存存储能力")
    func testFakeStorage() {
        let storage = FakeStorage()
        storage.set("token_123456", forKey: "auth_token")
        #expect(storage.string(forKey: "auth_token") == "token_123456")

        storage.set(true, forKey: "is_logged_in")
        #expect(storage.bool(forKey: "is_logged_in") == true)

        storage.removeObject(forKey: "auth_token")
        #expect(storage.string(forKey: "auth_token") == nil)
    }

    @Test("验证 FakeAPIClient 网络模拟请求")
    func testFakeAPIClient() async throws {
        struct MockUser: Codable, Sendable {
            let name: String
        }
        struct MockEndpoint: Endpoint {
            var baseURL: URL { URL(string: "https://api.example.com")! }
            var path: String { "users" }
            var method: HTTPMethod { .get }
        }

        let mockData = try JSONEncoder().encode(MockUser(name: "Bob"))
        let fakeClient = FakeAPIClient(mockResult: .success(mockData))

        let result: MockUser = try await fakeClient.request(MockEndpoint())
        #expect(result.name == "Bob")
    }
}
