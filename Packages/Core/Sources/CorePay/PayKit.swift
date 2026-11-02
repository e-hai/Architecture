import Foundation
import StoreKit
import CoreLog

/// 订阅与内购商品状态描述。
public struct CustomerInfo: Sendable {
    public let activeSubscriptions: Set<String>
    public let purchasedNonConsumables: Set<String>

    public init(
        activeSubscriptions: Set<String> = [],
        purchasedNonConsumables: Set<String> = []
    ) {
        self.activeSubscriptions = activeSubscriptions
        self.purchasedNonConsumables = purchasedNonConsumables
    }

    public var isPremium: Bool {
        !activeSubscriptions.isEmpty || !purchasedNonConsumables.isEmpty
    }
}

/// 基于 StoreKit 2 的内购与订阅管理门面。
public final class PayKit: @unchecked Sendable {
    public static let shared = PayKit()

    private init() {}

    /// 获取当前用户有效订阅与购买状态
    public func getCustomerInfo() async -> CustomerInfo {
        var activeSubs = Set<String>()
        var purchasedNonConsumables = Set<String>()

        for await result in Transaction.currentEntitlements {
            guard case .verified(let transaction) = result else { continue }
            if transaction.productType == .autoRenewable || transaction.productType == .nonRenewable {
                activeSubs.insert(transaction.productID)
            } else if transaction.productType == .nonConsumable {
                purchasedNonConsumables.insert(transaction.productID)
            }
        }

        LogKit.i("获取到用户有效购买: \(activeSubs), 非消耗品: \(purchasedNonConsumables)", tag: .pay)
        return CustomerInfo(
            activeSubscriptions: activeSubs,
            purchasedNonConsumables: purchasedNonConsumables
        )
    }

    /// 恢复购买
    public func restorePurchases() async throws {
        try await AppStore.sync()
        LogKit.i("已完成 AppStore 同步与购买恢复", tag: .pay)
    }
}
