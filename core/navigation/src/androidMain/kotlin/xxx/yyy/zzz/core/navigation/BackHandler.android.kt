package xxx.yyy.zzz.core.navigation

import androidx.compose.runtime.Composable

/**
 * Android 端系统返回键拦截实现（委托给 AndroidX Activity Compose BackHandler）。
 */
@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
