package xxx.yyy.zzz.core.ads

/**
 * iOS 端广告桥接协议。
 */
interface AdsIosBridge : AdsDelegate

/**
 * 供 iOS Swift 宿主注册原生 AdsKit-iOS 桥接。
 */
fun registerAdsIosBridge(bridge: AdsIosBridge) {
    AdsManager.install(bridge)
}
