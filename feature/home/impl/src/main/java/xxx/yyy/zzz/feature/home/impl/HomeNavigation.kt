package xxx.yyy.zzz.feature.home.impl

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xxx.yyy.zzz.feature.home.api.HomeNavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onSettingsClick: () -> Unit,
) {
    entry<HomeNavKey> {
        HomeRoute(onSettingsClick = onSettingsClick)
    }
}
