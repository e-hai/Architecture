package xxx.yyy.zzz.android.bridge.delegates

import android.content.Context
import com.kit.crashreport.CrashBackend
import com.kit.crashreport.CrashReport
import com.kit.crashreport.CrashReportConfig
import xxx.yyy.zzz.core.crashreport.CrashReportDelegate
import xxx.yyy.zzz.core.crashreport.CrashReportKeys
import xxx.yyy.zzz.core.log.CoreLog

/**
 * Android 崩溃分析委托实现（1:1 透传 CrashReportKit-Android 原生 SDK）。
 */
internal class AndroidCrashReportDelegate(
    private val context: Context,
    private val debug: Boolean,
) : CrashReportDelegate {
    override fun doInit() {
        runCatching {
            CrashReport.init(
                context = context,
                config =
                    CrashReportConfig(
                        enabled = true,
                        collectionEnabled = true,
                        backend = CrashBackend.FIREBASE,
                    ),
            )
            CrashReport.setCustomKey(CrashReportKeys.IS_DEBUG, debug)
        }.onFailure {
            CoreLog.w("CrashReport", "CrashReport init skipped or failed: ${it.message}")
        }
    }

    override fun setUserId(userId: String) = CrashReport.setUserId(userId)

    override fun setCustomKey(
        key: String,
        value: Any,
    ) {
        when (value) {
            is Boolean -> CrashReport.setCustomKey(key, value)
            is Int -> CrashReport.setCustomKey(key, value)
            is Long -> CrashReport.setCustomKey(key, value)
            is Float -> CrashReport.setCustomKey(key, value)
            is Double -> CrashReport.setCustomKey(key, value)
            else -> CrashReport.setCustomKey(key, value.toString())
        }
    }

    override fun log(message: String) = CrashReport.log(message)

    override fun recordException(throwable: Throwable) = CrashReport.recordException(throwable)
}
