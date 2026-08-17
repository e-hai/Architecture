package com.shortvideo.app.feature.discover.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.Brush
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
 * 方案 D（画报杂志生活美学流）发现与探索中心 Composable。
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
                .background(Color(0xFF0E0D0C))
                .statusBarsPadding(),
    ) {
        // 1. 顶部画报品牌标与搜索栏
        DiscoverHeader(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onClear = { viewModel.onSearchQueryChange("") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // 2. 探索内容瀑布流（编辑画报精选 + 话题 Chips + 双列画报展陈）
        if (uiState.isLoading && uiState.videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color(0xFFE5C384), strokeWidth = 2.5.dp)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 12.dp,
            ) {
                // 顶部编辑画报精选大卡 (Curated Spotlight)
                item(span = StaggeredGridItemSpan.FullLine) {
                    CuratedSpotlightCard(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                    )
                }

                // 话题筛选条
                item(span = StaggeredGridItemSpan.FullLine) {
                    TopicFilterRow(
                        categories = uiState.topics,
                        selectedTopic = uiState.selectedTopic ?: "全部",
                        onTopicSelect = viewModel::onTopicSelect,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                // 双列画报视频流
                items(uiState.videos, key = { it.id }) { video ->
                    EditorialVideoCard(
                        video = video,
                        onClick = { onVideoClick(video.id) },
                    )
                }
            }
        }
    }
}

/**
 * 顶部画报品牌与搜索框。
 */
@Composable
private fun DiscoverHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "EXPLORE · 探索画报",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFAF6EE),
                letterSpacing = 1.sp,
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "画报精选",
                tint = Color(0xFFE5C384),
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(46.dp),
            placeholder = {
                Text(
                    text = "搜索画报、创作者或美学灵感",
                    fontSize = 12.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.4f),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color(0xFFE5C384),
                    modifier = Modifier.size(18.dp),
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空",
                            tint = Color(0xFFFAF6EE).copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(23.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF181716),
                    unfocusedContainerColor = Color(0xFF151413),
                    focusedTextColor = Color(0xFFFAF6EE),
                    unfocusedTextColor = Color(0xFFFAF6EE),
                    focusedBorderColor = Color(0xFFE5C384).copy(alpha = 0.5f),
                    unfocusedBorderColor = Color(0xFF282624),
                ),
        )
    }
}

/**
 * 顶部画报编辑精选大卡 (Curated Spotlight Hero)。
 */
@Composable
private fun CuratedSpotlightCard(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .aspectRatio(2.1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF181716))
                .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
    ) {
        AsyncImage(
            model = "https://picsum.photos/800/400?random=spotlight",
            contentDescription = "本周精选画报",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // 渐变暗角遮罩
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.35f),
                                ),
                        ),
                    ),
        )

        // 期刊标语与探索
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE5C384).copy(alpha = 0.2f))
                        .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "ISSUE 04 · 本周画报精选",
                    fontSize = 10.sp,
                    color = Color(0xFFE5C384),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                )
            }

            Column {
                Text(
                    text = "光影与极简秘境之旅",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAF6EE),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "定格生活中的艺术瞬间 · 沉浸式视听策展",
                    fontSize = 11.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.75f),
                )
            }
        }
    }
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
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF161514),
                        labelColor = Color(0xFFFAF6EE).copy(alpha = 0.65f),
                        selectedContainerColor = Color(0xFFE5C384),
                        selectedLabelColor = Color(0xFF0E0D0C),
                    ),
                shape = RoundedCornerShape(16.dp),
                border = null,
            )
        }
    }
}

/**
 * 方案 D（画报生活美学）双列探索媒体卡片。
 */
@Composable
private fun EditorialVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141312))
                .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
    ) {
        // 封面图 + 顶部左侧画报标签
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .background(Color(0xFF1C1A18)),
        ) {
            AsyncImage(
                model = video.coverUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // 画报标签微胶囊
            val firstTag = video.tags.firstOrNull() ?: "CURATED"
            Box(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xBB0E0D0C))
                        .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
            ) {
                Text(
                    text = firstTag.uppercase(),
                    fontSize = 9.sp,
                    color = Color(0xFFE5C384),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
            }
        }

        // 视频标题与创作者
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = video.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFAF6EE),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
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
                                .clip(CircleShape)
                                .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.6f), CircleShape),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = video.author.nickname,
                        fontSize = 10.sp,
                        color = Color(0xFFFAF6EE).copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "点赞",
                        tint = Color(0xFFE5C384).copy(alpha = 0.7f),
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${video.likeCount}",
                        fontSize = 10.sp,
                        color = Color(0xFFFAF6EE).copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
