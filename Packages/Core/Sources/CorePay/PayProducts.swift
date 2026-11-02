import Foundation

/// 集中管理跨模块内购与订阅商品 ID。
public enum PayProducts {
    /// 基础月度订阅
    public static let monthlyPro = "com.architecture.subscription.monthly"
    /// 年度高级订阅
    public static let yearlyPro = "com.architecture.subscription.yearly"
    /// 终身买断非消耗品
    public static let lifetimePro = "com.architecture.inapp.lifetime"
}
