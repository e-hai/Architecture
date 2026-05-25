package xxx.yyy.zzz.feature.home.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import xxx.yyy.zzz.core.ui.LoadingView

@Composable
fun HomeRoute(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onSyncClick = viewModel::onSyncClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome to Modern Android!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onSettingsClick) {
            Text(text = "Go to Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is HomeUiState.Loading -> {
                LoadingView()
            }
            is HomeUiState.Success -> {
                Text(text = "User Name: ${uiState.user.name}")
                Text(text = "User Email: ${uiState.user.email}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onSyncClick() }) {
                    Text(text = "Sync User Data")
                }
            }
            is HomeUiState.Error -> {
                Text(text = "Error: ${uiState.message}")
            }
        }
    }
}
