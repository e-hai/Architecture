package xxx.yyy.zzz.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import xxx.yyy.zzz.shared.di.AppBootstrap

/**
 * 供 iOS 宿主启动时显式调用初始化 Koin 容器。
 */
fun initKoinIos() {
    AppBootstrap.doInitKoin()
}

/**
 * 导出供 iOS 宿主呈现的根视图控制器。
 */
@Suppress("FunctionName")
fun MainViewController(): UIViewController {
    initKoinIos()
    return ComposeUIViewController(configure = { enforceStrictPlistSanityCheck = false }) {
        App()
    }
}
