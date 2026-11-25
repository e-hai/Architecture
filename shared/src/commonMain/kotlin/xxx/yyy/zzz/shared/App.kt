@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
// [KIT:mmp:START]
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.mmp.CoreMmp
import xxx.yyy.zzz.core.mmp.MmpDeepLink
import xxx.yyy.zzz.shared.debug.DebugPanelBridge
// [KIT:mmp:END]
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableSet
import xxx.yyy.zzz.core.navigation.NavKey
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.ui.MyProjectTheme
import xxx.yyy.zzz.shared.di.AppBootstrap
import xxx.yyy.zzz.shared.navigation.AppNavGraph
import xxx.yyy.zzz.shared.navigation.defaultStartKey
import xxx.yyy.zzz.shared.navigation.defaultTopLevelKeys

/**
 * 跨平台应用 Compose 根入口视图。
 * 与具体业务 Feature 解耦，起始路由通过 [defaultStartKey] 统一装配。
 */
@Composable
fun App(
    modifier: Modifier = Modifier,
    startKey: NavKey = defaultStartKey,
    topLevelKeys: ImmutableSet<NavKey> = defaultTopLevelKeys,
    autoInitDeferredKits: Boolean = true,
) {
    if (autoInitDeferredKits) {
        LaunchedEffect(Unit) {
            AppBootstrap.doInitDeferredKits()
        }
    }
    MyProjectTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            val navState =
                remember(startKey, topLevelKeys) {
                    NavigationState(
                        startKey = startKey,
                        topLevelKeys = topLevelKeys,
                    )
                }
            val navigator = remember(navState) { Navigator(navState) }
            // [KIT:mmp:START]
            val deepLinkScope = rememberCoroutineScope()
            DisposableEffect(navigator, deepLinkScope) {
                CoreMmp.setDeepLinkListener { deepLink ->
                    CoreLog.i(
                        LogTags.MMP,
                        "收到 DeepLink 分发: value=${deepLink.deepLinkValue}, isDeferred=${deepLink.isDeferred}",
                    )
                    resolveDeepLinkNavKey(deepLink)?.let { targetKey ->
                        deepLinkScope.launch {
                            navigator.navigate(targetKey)
                        }
                    }
                }
                onDispose {
                    CoreMmp.setDeepLinkListener(null)
                }
            }
            // [KIT:mmp:END]
            AppNavGraph(
                navigator = navigator,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

// [KIT:mmp:START]

/**
 * 从跨平台深度链接载荷中提取并归一化目标路由标识（优先读取 `route`/`target` 参数，其次解析 URI 路径）。
 */
internal fun extractDeepLinkRouteToken(deepLink: MmpDeepLink): String? {
    val rawTarget =
        deepLink.params["route"]?.takeIf { it.isNotBlank() }
            ?: deepLink.params["target"]?.takeIf { it.isNotBlank() }
            ?: deepLink.deepLinkValue?.takeIf { it.isNotBlank() }
            ?: return null

    val withoutQuery = rawTarget.substringBefore('?').substringBefore('#').trim()
    val withoutScheme =
        if ("://" in withoutQuery) {
            withoutQuery.substringAfter("://")
        } else {
            withoutQuery
        }
    return withoutScheme
        .trim('/')
        .substringAfterLast('/')
        .lowercase()
        .takeIf { it.isNotEmpty() }
}

/**
 * 将归因/深度链接载荷映射为跨平台导航路由 [NavKey]；未匹配已知路由时返回 null。
 */
internal fun resolveDeepLinkNavKey(deepLink: MmpDeepLink): NavKey? =
    when (extractDeepLinkRouteToken(deepLink)) {
        "home" -> defaultStartKey
        "debug" -> DebugPanelBridge.debugKey
        else -> null
    }
// [KIT:mmp:END]
