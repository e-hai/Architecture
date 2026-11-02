import Testing
@testable import AppLib

@Suite("App 顶层集成测试")
struct AppTests {
    @Test("验证全局依赖装配")
    func testAppDependencyGraph() {
        AppDependencyGraph.registerAll()
        // 确保装配不发生 crash
        #expect(true)
    }
}
