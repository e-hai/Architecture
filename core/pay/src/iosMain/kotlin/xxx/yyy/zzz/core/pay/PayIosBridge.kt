package xxx.yyy.zzz.core.pay

/**
 * iOS 端支付桥接协议。
 */
interface PayIosBridge : PayDelegate

/**
 * 供 iOS Swift 宿主注册原生 PayKit-iOS 桥接。
 */
fun registerPayIosBridge(bridge: PayIosBridge) {
    PayKit.install(bridge)
}
