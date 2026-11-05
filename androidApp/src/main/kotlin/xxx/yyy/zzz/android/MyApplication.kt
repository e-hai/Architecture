package xxx.yyy.zzz.android

import android.app.Application
import xxx.yyy.zzz.shared.di.AppBootstrap
import xxx.yyy.zzz.shared.di.init

/**
 * Android 独立宿主 Application 入口。
 * 通过跨平台统一的 AppBootstrap 启动器，一行代码完成所有 Kit 与 Koin 的标准化三阶段初始化。
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppBootstrap.init(this)
    }
}
