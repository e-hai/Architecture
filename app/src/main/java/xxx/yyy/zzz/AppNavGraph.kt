package xxx.yyy.zzz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import xxx.yyy.zzz.core.navigation.NavigationAction
import xxx.yyy.zzz.core.navigation.Navigator
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.impl.HomeRoute

@Composable
fun AppNavGraph(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    // Nav3: Back stack is a simple state-backed list of NavKey
    val backStack = remember { mutableStateListOf<NavKey>(HomeNavKey) }

    // Observe navigation actions emitted by Navigator singleton
    LaunchedEffect(Unit) {
        navigator.actions.collect { action ->
            when (action) {
                is NavigationAction.Navigate -> {
                    backStack.add(action.destination)
                }
                is NavigationAction.Pop -> {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }
                }
            }
        }
    }

    val myEntryProvider = entryProvider<NavKey> {
        entry<HomeNavKey> {
            HomeRoute()
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            }
        },
        entryProvider = myEntryProvider,
    )
}
