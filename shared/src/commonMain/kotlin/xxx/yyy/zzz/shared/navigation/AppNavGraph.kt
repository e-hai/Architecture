package xxx.yyy.zzz.shared.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xxx.yyy.zzz.core.abtesting.AbTestingClient
import xxx.yyy.zzz.core.abtesting.AbTestingKeys
import xxx.yyy.zzz.core.analytics.Analytics
import xxx.yyy.zzz.core.analytics.AnalyticsEvents
import xxx.yyy.zzz.core.log.AppLog
import xxx.yyy.zzz.core.log.LogTags
import xxx.yyy.zzz.core.navigation.Navigator

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
                    Analytics.logEvent(AnalyticsEvents.Common.BUTTON_CLICK)
                    AppLog.d(LogTags.NAV, "Navigate to Settings")
                    navigator.navigate(SettingsNavKey)
                },
                modifier = modifier,
            )
        }

        is SettingsNavKey -> {
            SettingsScreen(
                onBack = {
                    AppLog.d(LogTags.NAV, "Go back from Settings")
                    navigator.goBack()
                },
                modifier = modifier,
            )
        }

        else -> {
            HomeScreen(
                onNavigateToSettings = { navigator.navigate(SettingsNavKey) },
                modifier = modifier,
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val welcomeMessage = AbTestingClient.getString(AbTestingKeys.WELCOME_MESSAGE, "Welcome to KMP Architecture")

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
            text = "Compose Multiplatform · JetBrains 2026 New Default Structure",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToSettings) {
            Text("Go to Settings")
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Settings Screen",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
