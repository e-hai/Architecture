package xxx.yyy.zzz.core.navigation

import androidx.compose.runtime.Composable

/**
 * 跨平台系统返回键/手势拦截抽象。
 */
@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
