package xxx.yyy.zzz.core.navigation

import androidx.compose.runtime.Composable

/**
 * iOS 端系统返回键拦截实现（iOS 无系统硬件返回键，由导航手势与顶部按钮驱动）。
 */
@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // iOS 平台无系统物理返回键
}
