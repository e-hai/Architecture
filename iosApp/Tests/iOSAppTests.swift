import Testing
@testable import iosAppLib

@Suite("iOS App Host & Kit Bridge Tests")
struct IOSAppTests {
    @Test("Bootstrapper installAll runs safely")
    func testBootstrapInstallAll() {
        KmpKitBridgeBootstrapper.installAll()
    }

    @Test("iOSAppView initializes correctly")
    @MainActor
    func testViewInitialization() {
        _ = iOSAppView()
    }
}
