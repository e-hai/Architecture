package xxx.yyy.zzz.core.abtesting

/**
 * iOS 端 A/B 测试桥接协议。
 */
interface AbTestingIosBridge : AbTestingDelegate

/**
 * 供 iOS Swift 宿主注册原生 AbTestingKit-iOS 桥接。
 */
fun registerAbTestingIosBridge(bridge: AbTestingIosBridge) {
    AbTestingClient.install(bridge)
}
