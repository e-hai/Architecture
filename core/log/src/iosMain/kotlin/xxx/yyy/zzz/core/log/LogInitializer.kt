package xxx.yyy.zzz.core.log

import kotlinx.cinterop.ExperimentalForeignApi
import xxx.yyy.zzz.core.log.interop.KitLogFacade

/**
 * iOS 端日志系统初始化器。
 * 通过 cinterop 直连工程内 Swift 门面 (KitLogFacade)，底层透传至 e-hai/LogKit-iOS 原生 SDK。
 */
@OptIn(ExperimentalForeignApi::class)
object LogInitializer {
    /**
     * 初始化 iOS 原生日志组件并挂载到跨平台 LogKit 门面。
     *
     * @param withDisk 是否开启磁盘日志文件记录
     */
    fun initialize(withDisk: Boolean = true) {
        KitLogFacade.initializeWithDisk(withDisk)

        LogKit.install(
            object : LogDelegate {
                override fun d(
                    tag: String,
                    message: String,
                ) {
                    KitLogFacade.d(tag, message)
                }

                override fun i(
                    tag: String,
                    message: String,
                ) {
                    KitLogFacade.i(tag, message)
                }

                override fun w(
                    tag: String,
                    message: String,
                ) {
                    KitLogFacade.w(tag, message)
                }

                override fun e(
                    tag: String,
                    message: String,
                    throwable: Throwable?,
                ) {
                    KitLogFacade.e(tag, message, throwable?.message)
                }
            },
        )
    }
}
