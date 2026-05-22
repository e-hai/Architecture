package xxx.yyy.zzz.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface NavigationAction {
    data object Pop : NavigationAction
    data class Navigate(val destination: NavKey) : NavigationAction
}

class Navigator {
    private val _actions = Channel<NavigationAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun navigate(destination: NavKey) {
        _actions.trySend(NavigationAction.Navigate(destination))
    }

    fun pop() {
        _actions.trySend(NavigationAction.Pop)
    }
}
