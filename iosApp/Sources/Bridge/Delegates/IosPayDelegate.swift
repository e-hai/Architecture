import Foundation
import PayKit

#if canImport(Shared)
import Shared

/**
 * iOS 支付结算委托实现（1:1 透传 PayKit-iOS 原生 SDK）。
 */
internal final class IosPayDelegate: NSObject, PayDelegate, @unchecked Sendable {
    @MainActor private var cachedProducts: [String: StoreProduct] = [:]

    func doInit(catalog: PayProductCatalog) {
        PayKit.setLogger(
            ClosurePayKitLogger { level, tag, message, error in
                let formatted = "[\(tag)] \(message)\(error.map { " error=\($0.localizedDescription)" } ?? "")"
                switch level {
                case .debug:
                    CoreLog.shared.d(tag: LogTags.shared.APP, message: formatted)
                case .info:
                    CoreLog.shared.i(tag: LogTags.shared.APP, message: formatted)
                case .warn:
                    CoreLog.shared.w(tag: LogTags.shared.APP, message: formatted)
                case .error:
                    CoreLog.shared.e(tag: LogTags.shared.APP, message: formatted, throwable: nil)
                }
            }
        )
        let config = PayKitConfiguration(
            subsProductIds: catalog.subsProductIds,
            consumableProductIds: catalog.consumableProductIds,
            nonConsumableProductIds: catalog.nonConsumableProductIds
        )
        PayKit.initialize(configuration: config)
    }

    func fetchProducts(productIds: Set<String>, onResult: @escaping ([PayProductInfo], String?) -> Void) {
        if productIds.isEmpty {
            onResult([], "No productIds configured")
            return
        }
        Task { @MainActor in
            let result = await PayKit.shared.getProducts(productIds: productIds)
            switch result {
            case .success(let storeProducts):
                for product in storeProducts {
                    self.cachedProducts[product.productId] = product
                }
                let mapped = storeProducts.map { product in
                    PayProductInfo(
                        productId: product.productId,
                        type: product.type == .subs ? PayProductType.subs : PayProductType.inapp,
                        title: product.title,
                        description: product.description,
                        formattedPrice: product.price,
                        priceAmountMicros: product.priceAmountMicros,
                        priceCurrencyCode: product.priceCurrencyCode,
                        basePlanId: nil,
                        subscriptionPeriod: product.subscriptionPeriod,
                        hasFreeTrial: product.hasFreeTrial,
                        freeTrialPeriod: product.freeTrialPeriod
                    )
                }
                onResult(mapped, nil)
            case .failure(let error):
                onResult([], error.message)
            }
        }
    }

    func purchase(productId: String, onResult: @escaping (KotlinBoolean, String?) -> Void) {
        Task { @MainActor in
            let productToBuy: StoreProduct?
            if let cached = self.cachedProducts[productId] {
                productToBuy = cached
            } else {
                let fetchResult = await PayKit.shared.getProducts(productIds: [productId])
                switch fetchResult {
                case .success(let products):
                    for item in products {
                        self.cachedProducts[item.productId] = item
                    }
                    productToBuy = products.first(where: { $0.productId == productId })
                case .failure(let error):
                    onResult(KotlinBoolean(bool: false), error.message)
                    return
                }
            }

            guard let storeProduct = productToBuy else {
                onResult(KotlinBoolean(bool: false), "Product not found in App Store: \(productId)")
                return
            }

            let purchaseResult = await PayKit.shared.purchase(storeProduct: storeProduct)
            switch purchaseResult {
            case .success:
                onResult(KotlinBoolean(bool: true), nil)
            case .failure(let error):
                onResult(KotlinBoolean(bool: false), error.message)
            }
        }
    }

    func isSubscribed(productId: String) -> Bool {
        return PayKit.shared.findActiveSubscription(productId: productId) != nil
    }

    func restorePurchases(onResult: @escaping (KotlinBoolean, String?) -> Void) {
        Task { @MainActor in
            let result = await PayKit.shared.restorePurchases()
            switch result {
            case .success:
                onResult(KotlinBoolean(bool: true), nil)
            case .failure(let error):
                onResult(KotlinBoolean(bool: false), error.message)
            }
        }
    }
}
#endif
