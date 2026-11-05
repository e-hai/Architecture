package xxx.yyy.zzz.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * 导出供 iOS 宿主呈现的根视图控制器。
 */
@Suppress("FunctionName")
fun MainViewController(): UIViewController =
    ComposeUIViewController(configure = { enforceStrictPlistSanityCheck = false }) {
        App()
    }
