package xxx.yyy.zzz.android.bridge.delegates

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.kit.log.LogKit
import xxx.yyy.zzz.core.log.LogDelegate
import xxx.yyy.zzz.core.log.LogTags

/**
 * Android 日志委托实现（1:1 透传 LogKit-Android 原生 SDK）。
 */
internal class AndroidLogDelegate(
    private val application: Application,
) : LogDelegate {
    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {
                    currentActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (currentActivity === activity) currentActivity = null
                }

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {
                    if (currentActivity === activity) currentActivity = null
                }
            },
        )
    }

    override fun doInitAllLog() {
        LogKit.initAllLog(application)
    }

    override fun doInitOnlyConsoleLog() {
        LogKit.initOnlyAndroidLog()
    }

    override fun doInitOnlyDiskLog() {
        LogKit.initOnlyDiskLog()
    }

    override fun v(
        tag: String,
        message: String,
    ) = LogKit.v(tag, message)

    override fun d(
        tag: String,
        message: String,
    ) = LogKit.d(tag, message)

    override fun i(
        tag: String,
        message: String,
    ) = LogKit.i(tag, message)

    override fun w(
        tag: String,
        message: String,
    ) = LogKit.w(tag, message)

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable != null) {
            LogKit.e(tag, throwable, message)
        } else {
            LogKit.e(tag, message)
        }
    }

    override fun wtf(
        tag: String,
        message: String,
    ) = LogKit.wtf(tag, message)

    override fun json(
        tag: String,
        json: String,
    ) = LogKit.json(tag, json)

    override fun xml(
        tag: String,
        xml: String,
    ) = LogKit.xml(tag, xml)

    override fun readLog(callback: (List<String>) -> Unit) {
        LogKit.readLog { list ->
            callback(list)
        }
    }

    override fun showLogUi() {
        currentActivity?.let { activity ->
            LogKit.showLogUi(activity)
        } ?: LogKit.w(LogTags.APP, "[LogKit-Android] 当前无可用的前台 Activity，无法唤起原生日志页面")
    }
}
