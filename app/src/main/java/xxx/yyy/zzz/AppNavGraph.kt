package xxx.yyy.zzz

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import xxx.yyy.zzz.core.analytics.AnalyticsEvent
import xxx.yyy.zzz.core.analytics.AnalyticsHelper
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.navigation.toEntries
import xxx.yyy.zzz.feature.home.api.HomeDetailNavKey
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.api.HomeResultNavKey
import xxx.yyy.zzz.feature.home.impl.homeEntry
import xxx.yyy.zzz.feature.settings.api.AboutAppNavKey
import xxx.yyy.zzz.feature.settings.api.PrivacyPolicyNavKey
import xxx.yyy.zzz.feature.settings.api.SettingsNavKey
import xxx.yyy.zzz.feature.settings.api.UserAgreementNavKey
import xxx.yyy.zzz.feature.settings.impl.settingsEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val analyticsHelper = koinInject<AnalyticsHelper>()
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Intercept back press when at the root to show "press again to exit" toast
    BackHandler(enabled = navigationState.isAtRoot) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, R.string.common_exit_toast, Toast.LENGTH_SHORT).show()
        }
    }

    val myEntryProvider = entryProvider {
        homeEntry(
            onNavigate = { navigator.navigate(it) },
            onBack = { navigator.goBack() },
        )
        settingsEntry(
            onNavigate = { navigator.navigate(it) },
            onBack = { navigator.goBack() },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val key = navigationState.currentKey) {
                            is HomeNavKey -> stringResource(R.string.nav_home)
                            is SettingsNavKey -> stringResource(R.string.nav_settings)
                            is HomeDetailNavKey -> key.title
                            is HomeResultNavKey -> key.title
                            is UserAgreementNavKey -> stringResource(R.string.user_agreement_title)
                            is PrivacyPolicyNavKey -> stringResource(R.string.privacy_policy_title)
                            is AboutAppNavKey -> stringResource(R.string.about_app_title)
                            else -> stringResource(R.string.app_name)
                        }
                    )
                },
                navigationIcon = {
                    if (!navigationState.isAtTopLevel) {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (navigationState.isAtTopLevel) {
                NavigationBar {
                    navigationState.topLevelKeys.forEach { key ->
                        NavigationBarItem(
                            selected = key == navigationState.currentTopLevelKey,
                            onClick = {
                                // Track tab switch event
                                analyticsHelper.logEvent(
                                    AnalyticsEvent(
                                        name = "tab_switch",
                                        params = mapOf("tab_name" to key::class.simpleName.orEmpty())
                                    )
                                )
                                navigator.navigate(key)
                            },
                            icon = {
                                Icon(
                                    imageVector = when (key) {
                                        is HomeNavKey -> Icons.Default.Home
                                        is SettingsNavKey -> Icons.Default.Settings
                                        else -> Icons.Default.Home
                                    },
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    text = when (key) {
                                        is HomeNavKey -> stringResource(R.string.nav_home)
                                        is SettingsNavKey -> stringResource(R.string.nav_settings)
                                        else -> stringResource(android.R.string.unknownName)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        NavDisplay(
            entries = navigationState.toEntries(myEntryProvider),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
