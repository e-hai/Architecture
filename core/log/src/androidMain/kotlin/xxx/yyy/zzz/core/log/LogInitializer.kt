package xxx.yyy.zzz.core.log

import android.content.Context
import com.kit.log.LogKit as AndroidNativeLogKit

/**
 * Android 端日志初始化器，透传至 e-hai/LogKit-Android 原生 SDK。
 */
object LogInitializer {
    fun initialize(
        context: Context,
        withDisk: Boolean = true,
    ) {
        if (withDisk) {
            AndroidNativeLogKit.initAllLog(context)
        } else {
            AndroidNativeLogKit.initOnlyAndroidLog()
        }

        LogKit.install(
            object : LogDelegate {
                override fun d(
                    tag: String,
                    message: String,
                ) = AndroidNativeLogKit.d(tag, message)

                override fun i(
                    tag: String,
                    message: String,
                ) = AndroidNativeLogKit.i(tag, message)

                override fun w(
                    tag: String,
                    message: String,
                ) = AndroidNativeLogKit.w(tag, message)

                override fun e(
                    tag: String,
                    message: String,
                    throwable: Throwable?,
                ) {
                    if (throwable != null) {
                        AndroidNativeLogKit.e(tag, throwable, message)
                    } else {
                        AndroidNativeLogKit.e(tag, message)
                    }
                }
            },
        )
    }
}
