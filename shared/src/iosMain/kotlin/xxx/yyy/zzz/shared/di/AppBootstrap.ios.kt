package xxx.yyy.zzz.shared.di

import xxx.yyy.zzz.core.log.LogKit
import xxx.yyy.zzz.core.log.LogTags

/**
 * iOS 平台统一初始化扩展函数。
 * 与 Android 端的 [AppBootstrap.init] 保持设计规范对齐：
 * 1. 启动全局 Koin 依赖注入容器；
 * 2. 输出 iOS 应用冷启动完成就绪日志。
 */
fun AppBootstrap.initIos() {
    doInitKoin()
    LogKit.i(LogTags.APP, "Application bootstrap completed on iOS.")
}
