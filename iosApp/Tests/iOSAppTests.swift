import Shared
import Testing
@testable import iosApp

/**
 * iOS 宿主与 Shared.framework 原生 Kit 桥接集成单元测试套件（运行于 iOS Simulator 宿主进程）。
 */
@Suite("iOS App Host & Shared Kit Bridge Tests")
struct IOSAppTests {
    /**
     * 验证 NativeKitBootstrap.initKits 与真实 Shared.framework 核心门面（Core* + Ios*Delegate）可安全协同执行。
     */
    @Test("Bootstrapper initKits integrates with Shared Core facades")
    func testBootstrapInitKitsWithSharedFramework() {
        NativeKitBootstrap.initKits(debug: true)

        CoreLog.shared.i(tag: LogTags.shared.APP, message: "[iOSAppTests] CoreLog delegate active")
        let fallbackMessage = CoreAbTesting.shared.getString(
            key: AbTestingKeys.shared.WELCOME_MESSAGE,
            defaultValue: "fallback"
        )
        #expect(!fallbackMessage.isEmpty)
        #expect(!CoreAds.shared.isReady(triggerId: "test_trigger"))
    }

    /**
     * 验证 ContentView 与 ComposeView 在完整 iOS Simulator 环境下可正常完成实例化。
     */
    @Test("ContentView initializes with Shared ComposeView")
    @MainActor
    func testViewInitialization() {
        _ = ContentView()
    }
}
