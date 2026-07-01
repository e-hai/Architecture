package xxx.yyy.zzz.feature.home.impl

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.LocalResultEventBus
import org.koin.androidx.compose.koinViewModel
import xxx.yyy.zzz.feature.home.api.TitleEditResult

@Composable
fun HomeDetailRoute(
    title: String,
    onNavigateToEdit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeDetailViewModel = koinViewModel(),
) {
    // 从首页进入时，用传过来的标题初始化 ViewModel 状态
    LaunchedEffect(title) {
        viewModel.initialize(title)
    }

    // 同样收到编辑页的 TitleEditResult（ResultEventBus 按类型广播，当前 Route 也能收到）
    // 实际生效的是首页的监听（更新列表标题），此处保留以便后续校验数据一致性
    LocalResultEventBus.current.conflateAsState<TitleEditResult?>(null).value?.let {
        viewModel.onTitleChange(it.title)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeDetailScreen(
        uiState = uiState,
        onNavigateToEdit = onNavigateToEdit,
        modifier = modifier,
    )
}

@Composable
fun HomeDetailScreen(
    uiState: HomeDetailViewModel.HomeDetailUiState,
    onNavigateToEdit: () -> Unit,
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
            text = stringResource(R.string.detail_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = uiState.title,
            style = MaterialTheme.typography.bodyLarge,
        )

        Button(
            onClick = onNavigateToEdit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.result_edit_title))
        }
    }
}
