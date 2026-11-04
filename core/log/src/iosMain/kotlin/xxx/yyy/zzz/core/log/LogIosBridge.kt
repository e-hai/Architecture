package xxx.yyy.zzz.core.log

/**
 * iOS 端日志桥接协议。
 */
interface LogIosBridge : LogDelegate

/**
 * 供 iOS Swift 宿主注册原生 LogKit-iOS 桥接。
 */
fun registerLogIosBridge(bridge: LogIosBridge) {
    LogKit.install(bridge)
}
