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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
 * 发现/探索中心页面 Composable（热点聚合与结构化探索）。
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
        // 顶部统一样式搜索框
        SearchBarHeader(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onClear = { viewModel.onSearchQueryChange("") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // 主体结构化探索内容（Banner + 实时热搜榜 + 分类筛选 + 瀑布流推荐）
        if (uiState.isLoading && uiState.videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
            ) {
                // 1. 顶部活动 Banner（未在关键词搜索时显示）
                if (uiState.searchQuery.isBlank() && uiState.banners.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        DiscoverBannerCard(banner = uiState.banners.first())
                    }
                }

                // 2. 实时热搜榜单 Top 5（未在关键词搜索时显示）
                if (uiState.searchQuery.isBlank() && uiState.trendingTopics.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        TrendingTopicsCard(
                            topics = uiState.trendingTopics,
                            onTopicClick = viewModel::onTrendingTopicClick,
                        )
                    }
                }

                // 3. 话题分类横向选择条
                item(span = StaggeredGridItemSpan.FullLine) {
                    CategoryFilterRow(
                        categories = uiState.topics,
                        selectedTopic = uiState.selectedTopic ?: "全部",
                        onTopicSelect = viewModel::onTopicSelect,
                    )
                }

                // 4. 双列瀑布流视频卡片
                items(uiState.videos, key = { it.id }) { video ->
                    ExploreVideoCard(
                        video = video,
                        onClick = { onVideoClick(video.id) },
                    )
                }
            }
        }
    }
}

/**
 * 顶部搜索框组件。
 */
@Composable
private fun SearchBarHeader(
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
                text = "搜索感兴趣的视频、作者或话题...",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.45f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清空",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF181818),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
    )
}

/**
 * 发现页活动/专题 Banner 卡片。
 */
@Composable
private fun DiscoverBannerCard(
    banner: DiscoverBanner,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(2.4f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B)),
    ) {
        AsyncImage(
            model = banner.coverUrl,
            contentDescription = banner.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // 底部渐变蒙层
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF2C55))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = banner.tag,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = banner.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = banner.subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
        }
    }
}

/**
 * 实时热搜榜 Top 5 卡片。
 */
@Composable
private fun TrendingTopicsCard(
    topics: List<TrendingTopic>,
    onTopicClick: (TrendingTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141414))
                .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "热搜",
                tint = Color(0xFFFF2C55),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "实时热搜榜",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "热度飙升",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f),
            )
        }

        topics.take(5).forEach { topic ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onTopicClick(topic) }
                        .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 排名标识
                Text(
                    text = "${topic.rank}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        when (topic.rank) {
                            1 -> Color(0xFFFF2C55)
                            2 -> Color(0xFFFF6B00)
                            3 -> Color(0xFFFFB800)
                            else -> Color.White.copy(alpha = 0.4f)
                        },
                    modifier = Modifier.width(22.dp),
                )

                // 话题标题
                Text(
                    text = "#${topic.title}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                // 标签徽标 (热/新)
                if (topic.isHot) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFFF2C55).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                    ) {
                        Text(text = "热", color = Color(0xFFFF2C55), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (topic.isNew) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF00C853).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                    ) {
                        Text(text = "新", color = Color(0xFF00C853), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // 热度数值
                Text(
                    text = topic.hotScore,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.35f),
                )
            }
        }
    }
}

/**
 * 分类选择 Chips 栏。
 */
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedTopic: String,
    onTopicSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF181818),
                        labelColor = Color.White.copy(alpha = 0.7f),
                        selectedContainerColor = Color(0xFFFF2C55),
                        selectedLabelColor = Color.White,
                    ),
                shape = RoundedCornerShape(16.dp),
                border = null,
            )
        }
    }
}

/**
 * 瀑布流视频卡片。
 */
@Composable
private fun ExploreVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141414))
                .clickable(onClick = onClick),
    ) {
        // 封面图
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .background(Color(0xFF222222)),
        ) {
            AsyncImage(
                model = video.coverUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 标题与作者信息
        Column(modifier = Modifier.padding(8.dp)) {
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
                                .size(16.dp)
                                .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = video.author.nickname,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "点赞",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${video.likeCount}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
