package com.shortvideo.app.core.crashreport

import android.content.Context
import com.kit.crashreport.CrashBackend
import com.kit.crashreport.CrashReport
import com.kit.crashreport.CrashReportConfig

/**
 * CrashReportKit 初始化入口。
 * 集中配置后端与采集开关，避免 Application 散落 SDK 细节。
 */
object CrashReportInitializer {
    /**
     * 初始化崩溃上报。须在业务调用 [CrashReport] 之前执行一次。
     * 宿主须已配置 `google-services` / `google-services.json`（Kit 不初始化 FirebaseApp）。
     *
     * @param context Application Context
     * @param debug 为 true 时写入 debug 自定义键，便于控制台筛选
     * @param enabled 总开关；false 时全部 API 为空操作
     * @param collectionEnabled 是否允许后端上传；可按隐私同意在运行期再调 [CrashReport.setCollectionEnabled]
     */
    fun initialize(
        context: Context,
        debug: Boolean,
        enabled: Boolean = true,
        collectionEnabled: Boolean = true,
    ) {
        CrashReport.init(
            context = context,
            config =
                CrashReportConfig(
                    enabled = enabled,
                    collectionEnabled = collectionEnabled,
                    backend = CrashBackend.FIREBASE,
                ),
        )
        CrashReport.setCustomKey(CrashReportKeys.DEBUG, debug)
    }
}
