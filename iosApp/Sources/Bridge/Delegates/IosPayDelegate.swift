import Foundation
import PayKit

#if canImport(Shared)
import Shared

/**
 * iOS 支付结算委托实现（1:1 透传 PayKit-iOS 原生 SDK）。
 */
internal final class IosPayDelegate: NSObject, PayDelegate {
    func doInit() {
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
        PayKit.initialize(configuration: PayKitConfiguration())
    }

    func purchase(productId: String, onResult: @escaping (KotlinBoolean, String?) -> Void) {
        Task {
            let result = await PayKit.shared.syncPurchases()
            switch result {
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
}
#endif
