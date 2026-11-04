package xxx.yyy.zzz.core.crashreport

/**
 * iOS 端崩溃上报桥接协议。
 */
interface CrashReportIosBridge : CrashReportDelegate

/**
 * 供 iOS Swift 宿主注册原生 CrashReportKit-iOS 桥接。
 */
fun registerCrashReportIosBridge(bridge: CrashReportIosBridge) {
    CrashReport.install(bridge)
}
