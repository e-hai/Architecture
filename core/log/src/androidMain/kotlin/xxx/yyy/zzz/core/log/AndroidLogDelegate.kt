package xxx.yyy.zzz.core.log

import android.content.Context
import com.kit.log.LogKit as AndroidNativeLogKit

/**
 * Android 端 LogDelegate 唯一代理实现类。
 * 封装所有对底层 com.kit.log.LogKit AAR 的调用，对外部模块完全隐藏原生 SDK。
 *
 * @param context Android 上下文对象
 */
class AndroidLogDelegate(
    private val context: Context,
) : LogDelegate {
    override fun doInitAllLog() {
        AndroidNativeLogKit.initAllLog(context.applicationContext)
    }

    override fun doInitOnlyConsoleLog() {
        AndroidNativeLogKit.initOnlyAndroidLog()
    }

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
}
