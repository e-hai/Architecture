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
import androidx.navigation3.runtime.result.ResultEffect
import org.koin.androidx.compose.koinViewModel
import xxx.yyy.zzz.feature.home.api.TitleEditResult

@Composable
fun HomeDetailRoute(
    title: String,
    onNavigateToEdit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeDetailViewModel = koinViewModel()
) {
    // 监听 TitleEditResult 类型的返回事件
//    ResultEffect<TitleEditResult> { result ->
//        // 当图片编辑页 sendResult 且当前页重新可见时，此 Lambda 会被触发
//        // 此时通知 ViewModel 去更新对应 item 的数据
//        viewModel.onTitleChange(result.title)
//    }
    LocalResultEventBus.current.conflateAsState<TitleEditResult?>(null).value?.let {
        Log.d("HomeDetailRoute", "收到图片编辑页的返回数据：$it")
        viewModel.onTitleChange(it.title)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        Log.d("HomeDetailRoute", "初始化")
        viewModel.initialize(title)
    }

    DisposableEffect(Unit, {
        println("📊 : 我诞生了！(HashCode: ${this.hashCode()})")
        onDispose {
            println("🍂 : 我被拔掉了，执行销毁清理！")
        }
    })

    HomeDetailScreen(
        uiState = uiState,
        onNavigateToEdit = onNavigateToEdit,
        modifier = modifier
    )
}

@Composable
fun HomeDetailScreen(
    uiState: HomeDetailViewModel.HomeDetailUiState,
    onNavigateToEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = uiState.title,
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = onNavigateToEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.result_edit_title))
        }
    }
}