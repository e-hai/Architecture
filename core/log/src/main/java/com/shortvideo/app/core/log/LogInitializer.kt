package com.shortvideo.app.core.log

import android.content.Context
import com.kit.log.LogKit

/**
 * LogKit 初始化入口。
 * Debug 默认 Logcat + 磁盘（可唤起应用内控制台）；Release 默认仅 Logcat。
 */
object LogInitializer {
    /**
     * 初始化日志框架。建议在 Application 最早阶段调用。
     *
     * @param context Application Context
     * @param withDisk 为 true 时启用磁盘持久化与 [LogKit.showLogUi] 控制台（一般仅 Debug）
     */
    fun initialize(
        context: Context,
        withDisk: Boolean = true,
    ) {
        if (withDisk) {
            LogKit.initAllLog(context)
        } else {
            LogKit.initOnlyAndroidLog()
        }
    }
}
