package xxx.yyy.zzz.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import xxx.yyy.zzz.shared.di.AppBootstrap
import xxx.yyy.zzz.shared.di.initIos

/**
 * 供 iOS 宿主启动时显式调用完成统一初始化流程。
 */
fun initAppIos() {
    AppBootstrap.initIos()
}

/**
 * 导出供 iOS 宿主呈现的根视图控制器。
 */
@Suppress("FunctionName")
fun MainViewController(): UIViewController {
    initAppIos()
    return ComposeUIViewController(configure = { enforceStrictPlistSanityCheck = false }) {
        App()
    }
}
