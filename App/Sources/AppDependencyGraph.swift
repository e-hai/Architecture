import Foundation
import CoreDI
import CoreNetwork
import CoreStorage
import CoreAnalytics
import CoreAds
import CorePay
import CoreLog
import CoreAbTesting

/// 全局服务装配中心。
/// 严格在应用启动阶段完成所有核心模块与基础设施的装配，提供给所有 Feature 模块无感消费。
public enum AppDependencyGraph {
    /// 注册与装配全局依赖
    public static func registerAll() {
        LogKit.i("开始装配全局依赖关系 (AppDependencyGraph)...", tag: .app)

        // 1. 注册核心网络
        DependencyContainer.shared.register(APIClientProtocol.self, scope: .singleton) {
            APIClient()
        }

        // 2. 注册本地持久化存储
        DependencyContainer.shared.register(KeyValueStorageProtocol.self, scope: .singleton) {
            UserDefaultsStorage()
        }

        // 3. 初始化商业化 Kit
        AdsManager.shared.initialize()
        AbTestingClient.shared.setDefaults(AbTestingKeys.defaultValues)

        LogKit.i("全局依赖装配完成", tag: .app)
    }
}
