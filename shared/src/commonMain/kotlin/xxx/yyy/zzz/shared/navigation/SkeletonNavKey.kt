package xxx.yyy.zzz.shared.navigation

import kotlinx.serialization.Serializable
import xxx.yyy.zzz.core.navigation.NavKey

/**
 * 首页路由 Key。
 */
@Serializable
data object HomeNavKey : NavKey

/**
 * 设置页路由 Key。
 */
@Serializable
data object SettingsNavKey : NavKey

/**
 * 开发者调试面板路由 Key。
 */
@Serializable
data object DebugNavKey : NavKey
