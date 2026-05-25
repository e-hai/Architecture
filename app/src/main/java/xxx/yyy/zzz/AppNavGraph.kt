package xxx.yyy.zzz

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import xxx.yyy.zzz.core.navigation.NavigationState
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.core.navigation.toEntries
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.impl.homeEntry
import xxx.yyy.zzz.feature.settings.api.SettingsNavKey
import xxx.yyy.zzz.feature.settings.impl.settingsEntry

@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Intercept back press when at the root to show "press again to exit" toast
    BackHandler(enabled = navigationState.isAtRoot) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "再次点击退出应用", Toast.LENGTH_SHORT).show()
        }
    }

    val myEntryProvider = entryProvider {
        homeEntry(
            onSettingsClick = { navigator.navigate(SettingsNavKey) }
        )
        settingsEntry {
            navigator.navigate(it)
        }
    }

    Scaffold(
        bottomBar = {
            if (navigationState.currentKey in navigationState.topLevelKeys) {
                NavigationBar {
                    navigationState.topLevelKeys.forEach { key ->
                        NavigationBarItem(
                            selected = key == navigationState.currentTopLevelKey,
                            onClick = { navigator.navigate(key) },
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
                                        is HomeNavKey -> "Home"
                                        is SettingsNavKey -> "Settings"
                                        else -> "Unknown"
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
