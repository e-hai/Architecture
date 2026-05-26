package xxx.yyy.zzz.feature.home.impl

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.result.LocalResultEventBus
import xxx.yyy.zzz.feature.home.api.HomeDetailNavKey
import xxx.yyy.zzz.feature.home.api.HomeNavKey
import xxx.yyy.zzz.feature.home.api.TitleEditResult


fun EntryProviderScope<NavKey>.homeEntry(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    entry<HomeNavKey> {
        HomeRoute(
            onNavigateToEdit = { item ->
                onNavigate(HomeDetailNavKey(id = item.id, title = item.title))
            }
        )
    }

    entry<HomeDetailNavKey> { key ->
        val resultEventBus = LocalResultEventBus.current
        HomeDetailRoute(
            title = key.title,
            onNavigateBack = { newTitle ->
                resultEventBus.sendResult(TitleEditResult(key.id, newTitle))
                onBack()
            }
        )
    }
}
