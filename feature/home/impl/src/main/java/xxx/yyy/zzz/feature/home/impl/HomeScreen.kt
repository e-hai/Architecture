package xxx.yyy.zzz.feature.home.impl

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import org.koin.androidx.compose.koinViewModel
import xxx.yyy.zzz.core.model.ListItem
import xxx.yyy.zzz.feature.home.api.TitleEditResult

@Composable
fun HomeRoute(
    onNavigateToDetail: (ListItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {

    // 监听 TitleEditResult 类型的返回事件
    LocalResultEventBus.current.conflateAsState<TitleEditResult?>(null).value?.let {
        viewModel.updateItemTitle(it.id,it.title)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    HomeScreen(
        uiState = uiState,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToDetail: (ListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_loading))
            }
        }

        is HomeUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Horizontal List Section
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.home_featured_content),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.featuredItems) { item ->
                                FeaturedCard(
                                    title = item.title,
                                    onClick = { onNavigateToDetail(item) }
                                )
                            }
                        }
                    }
                }

                // Vertical List Section
                item {
                    Text(
                        text = stringResource(R.string.home_recent_updates),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                items(uiState.recentItems) { item ->
                    RegularItem(
                        title = item.title,
                        onClick = { onNavigateToDetail(item) }
                    )
                }
            }
        }

        is HomeUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_error, uiState.message),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun FeaturedCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 100.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun RegularItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        uiState = HomeUiState.Success(
            featuredItems = listOf(
                ListItem(id = 1, title = "Item 1"),
            ),
            recentItems = listOf(
                ListItem(id = 1, title = "Item 1"),
                ListItem(id = 2, title = "Item 2"),
                ListItem(id = 3, title = "Item 3"),
                ListItem(id = 4, title = "Item 4"),
            )
        ),
        onNavigateToDetail = {},
        modifier = Modifier
    )
}


