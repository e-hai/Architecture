package com.shortvideo.app.feature.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shortvideo.app.core.model.VideoItem
import org.koin.androidx.compose.koinViewModel

/**
 * 极简个人中心/用户主页 Composable。
 *
 * 遵循全球化极简设计原则：开阔留白、大气质感、信息层级清晰、操作直观。
 *
 * @param userId 用户 ID，为空时代表当前登录用户个人主页
 * @param onVideoClick 点击作品卡片进入播放流回调
 * @param onEditProfileClick 点击编辑资料回调
 * @param onSettingsClick 点击设置回调
 * @param viewModel 个人主页 ViewModel
 */
@Composable
fun ProfileScreen(
    userId: String? = null,
    onVideoClick: (String) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    val currentList =
        when (uiState.selectedTab) {
            0 -> uiState.works
            1 -> uiState.likedVideos
            else -> uiState.bookmarkedVideos
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalArrangement = Arrangement.spacedBy(1.5.dp),
    ) {
        // 头部个人信息与留白
        item(span = { GridItemSpan(3) }) {
            ProfileHeroSection(
                uiState = uiState,
                onEditProfileClick = onEditProfileClick,
                onSettingsClick = onSettingsClick,
            )
        }

        // Tab 栏
        item(span = { GridItemSpan(3) }) {
            ProfileTabs(
                selectedTab = uiState.selectedTab,
                worksCount = uiState.works.size,
                likedCount = uiState.likedVideos.size,
                bookmarksCount = uiState.bookmarkedVideos.size,
                onTabSelect = viewModel::onTabSelect,
            )
        }

        // 媒体内容网格
        if (uiState.isLoading && currentList.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp)
                }
            }
        } else if (currentList.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无内容",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }
            }
        } else {
            items(currentList, key = { it.id }) { video ->
                ProfileMediaCard(
                    video = video,
                    onClick = { onVideoClick(video.id) },
                )
            }
        }
    }
}

/**
 * 个人主页头部区域（大量留白与极简层次）。
 */
@Composable
private fun ProfileHeroSection(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 顶部操作栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 头像
        AsyncImage(
            model = uiState.user?.avatarUrl,
            contentDescription = uiState.user?.nickname,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 昵称与简介
        Text(
            text = uiState.user?.nickname ?: "创作者",
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = uiState.user?.bio ?: "记录生活的点滴与灵感 ✨",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.65f),
            lineHeight = 18.sp,
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 核心三项数据看板（获赞 · 关注 · 粉丝）
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroStatItem(count = formatNumber(uiState.totalLikedCount), label = "获赞")
            HeroStatItem(count = "${uiState.followingCount}", label = "关注")
            HeroStatItem(count = formatNumber(uiState.followerCount), label = "粉丝")
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 极简操作按钮（全宽药丸设计）
        Button(
            onClick = onEditProfileClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                ),
        ) {
            Text(
                text = "编辑资料",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun HeroStatItem(
    count: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}

/**
 * 极简 Tab 栏。
 */
@Composable
private fun ProfileTabs(
    selectedTab: Int,
    worksCount: Int,
    likedCount: Int,
    bookmarksCount: Int,
    onTabSelect: (Int) -> Unit,
) {
    val tabs = listOf("作品 $worksCount", "喜欢 $likedCount", "收藏 $bookmarksCount")

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Black,
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = Color.White,
                height = 2.dp,
            )
        },
        divider = {
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        },
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelect(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

/**
 * 3 列极简媒体卡片。
 */
@Composable
private fun ProfileMediaCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(0.75f)
                .background(Color(0xFF141414))
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // 底部播放数指示
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "播放数",
                tint = Color.White,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = formatNumber(video.likeCount),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun formatNumber(count: Long): String =
    when {
        count >= 10000 -> String.format("%.1fw", count / 10000.0)
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
