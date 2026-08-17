package com.shortvideo.app.feature.discover.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shortvideo.app.core.model.VideoItem
import org.koin.androidx.compose.koinViewModel

/**
 * 极简发现/探索中心页面 Composable。
 *
 * 遵循全球化极简设计哲学：去繁就简、大量留白、开阔呼吸感，入口直接明了。
 *
 * @param onVideoClick 点击视频卡片跳转播放流回调
 * @param viewModel 发现 ViewModel
 */
@Composable
fun DiscoverScreen(
    onVideoClick: (String) -> Unit = {},
    viewModel: DiscoverViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding(),
    ) {
        // 1. 顶部极简开阔搜索栏
        DiscoverSearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onClear = { viewModel.onSearchQueryChange("") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        )

        // 2. 探索内容瀑布流（话题 Chips + 双列媒体卡片）
        if (uiState.isLoading && uiState.videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 12.dp,
            ) {
                // 顶部留白与话题筛选条
                item(span = StaggeredGridItemSpan.FullLine) {
                    TopicFilterRow(
                        categories = uiState.topics,
                        selectedTopic = uiState.selectedTopic ?: "全部",
                        onTopicSelect = viewModel::onTopicSelect,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                // 双列探索视频流
                items(uiState.videos, key = { it.id }) { video ->
                    DiscoverVideoCard(
                        video = video,
                        onClick = { onVideoClick(video.id) },
                    )
                }
            }
        }
    }
}

/**
 * 极简搜索栏组件。
 */
@Composable
private fun DiscoverSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
        placeholder = {
            Text(
                text = "搜索视频、作者或话题",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.4f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清空",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161616),
                unfocusedContainerColor = Color(0xFF141414),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
    )
}

/**
 * 极简单行话题筛选条。
 */
@Composable
private fun TopicFilterRow(
    categories: List<String>,
    selectedTopic: String,
    onTopicSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(categories) { category ->
            val isSelected = (selectedTopic == category)
            FilterChip(
                selected = isSelected,
                onClick = { onTopicSelect(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF141414),
                        labelColor = Color.White.copy(alpha = 0.6f),
                        selectedContainerColor = Color.White,
                        selectedLabelColor = Color.Black,
                    ),
                shape = RoundedCornerShape(16.dp),
                border = null,
            )
        }
    }
}

/**
 * 极简双列探索媒体卡片。
 */
@Composable
private fun DiscoverVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF121212))
                .clickable(onClick = onClick),
    ) {
        // 封面图
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .background(Color(0xFF1E1E1E)),
        ) {
            AsyncImage(
                model = video.coverUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 视频标题与创作者
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = video.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    AsyncImage(
                        model = video.author.avatarUrl,
                        contentDescription = video.author.nickname,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = video.author.nickname,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "点赞",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${video.likeCount}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.45f),
                    )
                }
            }
        }
    }
}
