import Testing
@testable import iosAppLib

/**
 * iOS 宿主与原生 Kit 桥接单元测试套件。
 */
@Suite("iOS App Host & Kit Bridge Tests")
struct IOSAppTests {
    /**
     * 验证 NativeKitBootstrap.initKits 桥接批量安装逻辑安全执行且无异常。
     */
    @Test("Bootstrapper initKits runs safely")
    func testBootstrapInitKits() {
        NativeKitBootstrap.initKits()
    }

    /**
     * 验证 iOSAppView 作为可复用视图组件在沙盒环境下能正确完成实例化与生命周期装配。
     */
    @Test("iOSAppView initializes correctly")
    @MainActor
    func testViewInitialization() {
        _ = iOSAppView()
    }
}
