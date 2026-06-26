package xxx.yyy.zzz.feature.home.impl

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeResultRoute(
    title: String,
    onNavigateBack: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeResultViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(title) {
        viewModel.initialize(title)
    }

    HomeResultScreen(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onSaveClick = {
            Log.d("HomeRoute", "HomeResultScreen onSaveClick: ${uiState.editedTitle}")
            onNavigateBack(uiState.editedTitle)
        },
        modifier = modifier,
    )
}

@Composable
fun HomeResultScreen(
    uiState: HomeResultViewModel.HomeResultUiState,
    onTitleChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.result_edit_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = uiState.editedTitle,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.result_title_label)) },
        )

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.result_save))
        }
    }
}
