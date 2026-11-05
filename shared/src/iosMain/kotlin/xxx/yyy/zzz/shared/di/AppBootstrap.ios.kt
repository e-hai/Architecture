package xxx.yyy.zzz.shared.di

import xxx.yyy.zzz.core.log.LogInitializer
import xxx.yyy.zzz.core.log.LogKit
import xxx.yyy.zzz.core.log.LogTags

/**
 * iOS 宿主统一初始化扩展。
 * 严格按照跨平台标准三阶段完成冷启动流程：
 * 1. 批量初始化 iOS 原生 Kit（通过 cinterop 直连 Swift 门面 KitLogFacade）；
 * 2. 启动全局 Koin 依赖注入容器；
 * 3. 打印应用冷启动完成确认日志。
 */
fun AppBootstrap.initIos(withDiskLog: Boolean = true) {
    // Phase 1: 平台原生 Kit 初始化（通过 cinterop 触发 Swift 门面）
    LogInitializer.initialize(withDisk = withDiskLog)

    // Phase 2: Koin 依赖注入容器启动
    doInitKoin()

    // Phase 3: 就绪日志
    LogKit.i(LogTags.APP, "Application bootstrap completed on iOS via cinterop.")
}
