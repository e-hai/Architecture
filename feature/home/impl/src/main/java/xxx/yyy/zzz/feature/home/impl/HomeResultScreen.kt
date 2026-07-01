package xxx.yyy.zzz.feature.home.impl

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
    onSaveToDetail: (String) -> Unit,
    onSaveToHome: (String) -> Unit,
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
        onSaveToDetail = {
            onSaveToDetail(uiState.editedTitle)
        },
        onSaveToHome = {
            onSaveToHome(uiState.editedTitle)
        },
        modifier = modifier,
    )
}

@Composable
fun HomeResultScreen(
    uiState: HomeResultViewModel.HomeResultUiState,
    onTitleChange: (String) -> Unit,
    onSaveToDetail: () -> Unit,
    onSaveToHome: () -> Unit,
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
            onClick = onSaveToDetail,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.result_save_to_detail))
        }

        Button(
            onClick = onSaveToHome,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.result_save_to_home))
        }
    }
}
