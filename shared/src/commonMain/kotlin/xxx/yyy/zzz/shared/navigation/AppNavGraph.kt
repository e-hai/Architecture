package xxx.yyy.zzz.shared.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.abtesting.CoreAbTesting
import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.analytics.CoreAnalytics
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.navigation.BackHandler
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.ui.generated.resources.Res
import xxx.yyy.zzz.core.ui.generated.resources.back
import xxx.yyy.zzz.core.ui.generated.resources.go_to_settings
import xxx.yyy.zzz.core.ui.generated.resources.open_debug_console
import xxx.yyy.zzz.core.ui.generated.resources.settings_title
import xxx.yyy.zzz.core.ui.generated.resources.sub_title
import xxx.yyy.zzz.core.ui.generated.resources.welcome_message
import xxx.yyy.zzz.shared.debug.DebugScreen

/**
 * 跨平台路由分发图。
 */
@Composable
fun AppNavGraph(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val currentKey = navigator.state.currentKey

    when (currentKey) {
        is HomeNavKey -> {
            HomeScreen(
                onNavigateToSettings = {
                    CoreAnalytics.logEvent(AnalyticsEvents.Common.BUTTON_CLICK)
                    CoreLog.d(LogTags.NAV, "Navigate to Settings")
                    navigator.navigate(SettingsNavKey)
                },
                onNavigateToDebug = {
                    navigator.navigate(DebugNavKey)
                },
                modifier = modifier,
            )
        }

        is SettingsNavKey -> {
            SettingsScreen(
                onBack = {
                    CoreLog.d(LogTags.NAV, "Go back from Settings")
                    navigator.goBack()
                },
                modifier = modifier,
            )
        }

        is DebugNavKey -> {
            DebugScreen(
                onBack = {
                    CoreLog.d(LogTags.NAV, "Go back from Debug")
                    navigator.goBack()
                },
                modifier = modifier,
            )
        }

        else -> {
            HomeScreen(
                onNavigateToSettings = { navigator.navigate(SettingsNavKey) },
                onNavigateToDebug = { navigator.navigate(DebugNavKey) },
                modifier = modifier,
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDebug: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val appConfig: AppConfig = koinInject()
    val defaultWelcome = stringResource(Res.string.welcome_message)
    val welcomeMessage = CoreAbTesting.getString(AbTestingKeys.WELCOME_MESSAGE, defaultWelcome)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = welcomeMessage,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.sub_title),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToSettings) {
            Text(stringResource(Res.string.go_to_settings))
        }

        // 仅在 Debug 模式下展示开发者调试面板入口
        if (appConfig.isDebug) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onNavigateToDebug) {
                Text("🛠️ " + stringResource(Res.string.open_debug_console))
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text(stringResource(Res.string.back))
        }
    }
}
