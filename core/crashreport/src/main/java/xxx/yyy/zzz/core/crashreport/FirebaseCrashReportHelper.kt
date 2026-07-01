package xxx.yyy.zzz.core.crashreport

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

/**
 * Firebase Crashlytics 实现的崩溃上报助手。
 *
 * 封装 FirebaseCrashlytics 的具体逻辑，提供统一的崩溃记录接口。
 */
class FirebaseCrashReportHelper : CrashReportHelper {

    private val crashlytics by lazy {
        Firebase.crashlytics
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setUserId(userId: String?) {
        crashlytics.setUserId(userId ?: return)
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        crashlytics.setCustomKey(key, value)
    }

    override fun logBreadcrumb(name: String) {
        crashlytics.log("Breadcrumb: $name")
    }
}
