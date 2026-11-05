import Foundation

#if canImport(Shared)
import Shared

/**
 * AppBootstrap iOS 宿主便捷扩展。
 * 遵循与 Android 端 AppBootstrap.init(application) 相同的统一门面设计，
 * 提供一键完成 iOS 原生 Kit SPM Bridge 安装与跨平台 Koin 容器启动的标准入口。
 */
extension AppBootstrap {
    /**
     * iOS 宿主标准化冷启动一键初始化扩展方法。
     * 风格与 Android 端的 AppBootstrap.init(this) 保持设计规范对齐：
     * 1. 批量安装 8 大原生 Kit SPM 桥接实现 (KmpKitBridgeBootstrapper.installAll)；
     * 2. 执行 Kotlin 跨平台统一初始化扩展 (AppBootstrap.shared.doInitIos())。
     */
    public static func initIos() {
        KmpKitBridgeBootstrapper.installAll()
        AppBootstrap.shared.doInitIos()
    }
}
#endif
