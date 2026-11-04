package xxx.yyy.zzz.core.mmp

/**
 * iOS 端归因桥接协议。
 */
interface MmpIosBridge : MmpDelegate

/**
 * 供 iOS Swift 宿主注册原生 MmpKit-iOS 桥接。
 */
fun registerMmpIosBridge(bridge: MmpIosBridge) {
    MmpKit.install(bridge)
}
