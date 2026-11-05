package xxx.yyy.zzz.core.crashreport

import android.content.Context
import com.kit.crashreport.CrashBackend
import com.kit.crashreport.CrashReportConfig
import com.kit.crashreport.CrashReport as AndroidNativeCrashReport

/**
 * Android 端崩溃分析初始化器。
 */
object CrashReportInitializer {
    fun initialize(
        context: Context,
        debug: Boolean,
        enabled: Boolean = true,
        collectionEnabled: Boolean = true,
    ) {
        runCatching {
            AndroidNativeCrashReport.init(
                context = context,
                config =
                    CrashReportConfig(
                        enabled = enabled,
                        collectionEnabled = collectionEnabled,
                        backend = CrashBackend.FIREBASE,
                    ),
            )
            AndroidNativeCrashReport.setCustomKey(CrashReportKeys.DEBUG, debug)
        }.onFailure {
            android.util.Log.w("CrashReport", "CrashReport init skipped or failed: ${it.message}")
        }

        CrashReport.install(
            object : CrashReportDelegate {
                override fun setUserId(userId: String) = AndroidNativeCrashReport.setUserId(userId)

                override fun setCustomKey(
                    key: String,
                    value: Any,
                ) {
                    when (value) {
                        is Boolean -> AndroidNativeCrashReport.setCustomKey(key, value)
                        is Int -> AndroidNativeCrashReport.setCustomKey(key, value)
                        is Long -> AndroidNativeCrashReport.setCustomKey(key, value)
                        is Float -> AndroidNativeCrashReport.setCustomKey(key, value)
                        is Double -> AndroidNativeCrashReport.setCustomKey(key, value)
                        else -> AndroidNativeCrashReport.setCustomKey(key, value.toString())
                    }
                }

                override fun log(message: String) = AndroidNativeCrashReport.log(message)

                override fun recordException(throwable: Throwable) = AndroidNativeCrashReport.recordException(throwable)
            },
        )
    }
}
