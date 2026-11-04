package xxx.yyy.zzz.core.analytics

/**
 * iOS 端统计桥接协议。
 */
interface AnalyticsIosBridge : AnalyticsDelegate

/**
 * 供 iOS Swift 宿主注册原生 AnalyticsKit-iOS 桥接。
 */
fun registerAnalyticsIosBridge(bridge: AnalyticsIosBridge) {
    Analytics.install(bridge)
}
