package xxx.yyy.zzz.core.push

/**
 * iOS 端推送桥接协议。
 */
interface PushIosBridge : PushDelegate

/**
 * 供 iOS Swift 宿主注册原生 PushKit-iOS 桥接。
 */
fun registerPushIosBridge(bridge: PushIosBridge) {
    PushKitManager.install(bridge)
}
