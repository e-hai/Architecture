package xxx.yyy.zzz.feature.home.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xxx.yyy.zzz.core.ui.generated.resources.Res
import xxx.yyy.zzz.core.ui.generated.resources.open_debug_console
import xxx.yyy.zzz.core.ui.generated.resources.sub_title
import xxx.yyy.zzz.core.ui.generated.resources.welcome_message

/**
 * 首页呈现页面。
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToDebug: (() -> Unit)? = null,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val defaultWelcome = stringResource(Res.string.welcome_message)
    val title = uiState.welcomeMessage.ifBlank { defaultWelcome }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("home_title"),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.sub_title),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("home_subtitle"),
        )

        if (uiState.isDebug && onNavigateToDebug != null) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onNavigateToDebug,
                modifier = Modifier.testTag("home_open_debug_button"),
            ) {
                Text("🛠️ " + stringResource(Res.string.open_debug_console))
            }
        }
    }
}
