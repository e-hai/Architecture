package xxx.yyy.zzz.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import xxx.yyy.zzz.shared.di.AppBootstrap
import xxx.yyy.zzz.shared.di.NativeKitBootstrap

/**
 * Android 独立宿主 Application 入口。
 * 清晰分为「1. 原生 Kit 初始化」与「2. 跨平台 KMP 库初始化」两大标准步骤。
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 1. 初始化未实现跨平台的 Android 原生 Kit (8 大 AAR)
        NativeKitBootstrap.initKits(this)
        // 2. 初始化已实现跨平台的纯 KMP 库 (Koin: Room / DataStore / Ktor)
        AppBootstrap.doInitKoin {
            androidContext(this@MyApplication)
        }
    }
}
