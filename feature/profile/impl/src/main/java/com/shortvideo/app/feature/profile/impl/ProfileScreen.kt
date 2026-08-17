package com.shortvideo.app.feature.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
 * 方案 D（画报杂志生活美学流）个人主页 Composable。
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
                .background(Color(0xFF0E0D0C)),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // 头部沉浸画报与个人资料
        item(span = { GridItemSpan(3) }) {
            ProfileEditorialHeroSection(
                uiState = uiState,
                onEditProfileClick = onEditProfileClick,
                onSettingsClick = onSettingsClick,
            )
        }

        // 典雅 Tab 栏
        item(span = { GridItemSpan(3) }) {
            ProfileEditorialTabs(
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
                    CircularProgressIndicator(color = Color(0xFFE5C384), strokeWidth = 2.5.dp)
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
                        text = "暂无展陈画报作品",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFAF6EE).copy(alpha = 0.4f),
                    )
                }
            }
        } else {
            items(currentList, key = { it.id }) { video ->
                ProfileEditorialMediaCard(
                    video = video,
                    onClick = { onVideoClick(video.id) },
                )
            }
        }
    }
}

/**
 * 个人主页头部画报与资料区域。
 */
@Composable
private fun ProfileEditorialHeroSection(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 顶部沉浸全景画报 Banner
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF181716)),
        ) {
            AsyncImage(
                model = "https://picsum.photos/900/300?random=banner_curated",
                contentDescription = "画报背景",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // 底部渐变暗调过渡
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color(0xFF0E0D0C),
                                    ),
                            ),
                        ),
            )

            // 设置按钮
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = Color(0xFFFAF6EE),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // 悬浮交叠大头像（香槟金环）
        Box(
            modifier =
                Modifier
                    .offset(y = (-40).dp)
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0E0D0C))
                    .padding(3.dp)
                    .border(1.5.dp, Color(0xFFE5C384), CircleShape),
        ) {
            AsyncImage(
                model = uiState.user?.avatarUrl,
                contentDescription = uiState.user?.nickname,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
            )
        }

        // 昵称与简介
        Column(
            modifier =
                Modifier
                    .offset(y = (-32).dp)
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = uiState.user?.nickname ?: "CURATOR · 策展人",
                fontSize = 18.sp,
                color = Color(0xFFFAF6EE),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uiState.user?.bio ?: "记录生活的闪光瞬间 ✨ | 摄影 · 旅行 · 美学策展",
                fontSize = 12.sp,
                color = Color(0xFFFAF6EE).copy(alpha = 0.65f),
                lineHeight = 17.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 核心三项数据看板（获赞 · 关注 · 粉丝）
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141312))
                        .border(0.5.dp, Color(0xFFE5C384).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeroStatItem(count = formatNumber(uiState.totalLikedCount), label = "获赞 LIKES")
                HeroStatItem(count = "${uiState.followingCount}", label = "关注 FOLLOWING")
                HeroStatItem(count = formatNumber(uiState.followerCount), label = "粉丝 CURATORS")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 极简画报操作按钮
            Button(
                onClick = onEditProfileClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                shape = RoundedCornerShape(19.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF181716),
                        contentColor = Color(0xFFE5C384),
                    ),
                border =
                    ButtonDefaults.outlinedButtonBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFE5C384), Color(0xFFC7A76B))),
                    ),
            ) {
                Text(
                    text = "编辑画报主页 · EDIT PROFILE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
            }
        }
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
            fontSize = 15.sp,
            color = Color(0xFFFAF6EE),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFFE5C384).copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )
    }
}

/**
 * 极简画报 Tab 栏。
 */
@Composable
private fun ProfileEditorialTabs(
    selectedTab: Int,
    worksCount: Int,
    likedCount: Int,
    bookmarksCount: Int,
    onTabSelect: (Int) -> Unit,
) {
    val tabs = listOf("POSTS · 作品 $worksCount", "SERIES · 喜欢 $likedCount", "CURATIONS · 收藏 $bookmarksCount")

    SecondaryTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color(0xFF0E0D0C),
        contentColor = Color(0xFFE5C384),
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(selectedTab),
                color = Color(0xFFE5C384),
                height = 2.dp,
            )
        },
        divider = {
            HorizontalDivider(color = Color(0xFFE5C384).copy(alpha = 0.15f))
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
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFFE5C384) else Color(0xFFFAF6EE).copy(alpha = 0.45f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

/**
 * 3 列画报媒体卡片。
 */
@Composable
private fun ProfileEditorialMediaCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(0.75f)
                .background(Color(0xFF141312))
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
                tint = Color(0xFFE5C384),
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = formatNumber(video.likeCount),
                color = Color(0xFFFAF6EE),
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
