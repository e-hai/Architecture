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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * 极简现代个人主页 Composable。
 *
 * 遵循全球化主流短视频应用的极简原则：去除繁复冗余的背景装饰，强化留白，直观呈现核心个人信息、数据统计、拍摄入口与作品流。
 *
 * @param userId 用户 ID，为空时代表当前登录用户主页
 * @param onBackClick 二级页面返回回调（查看他人主页时生效）
 * @param onVideoClick 点击作品卡片进入播放流回调
 * @param onEditProfileClick 点击编辑资料回调
 * @param onRecordClick 点击拍摄创作回调
 * @param onSettingsClick 点击设置回调
 * @param viewModel 个人主页 ViewModel
 */
@Composable
fun ProfileScreen(
    userId: String? = null,
    onBackClick: (() -> Unit)? = null,
    onVideoClick: (String) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSelf = (userId == null)

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
        // 1. 顶部操作栏
        item(span = { GridItemSpan(3) }) {
            ProfileTopBar(
                isSelf = isSelf,
                nickname = uiState.user?.nickname.orEmpty(),
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick,
            )
        }

        // 2. 核心用户资料与操作入口
        item(span = { GridItemSpan(3) }) {
            ProfileHeaderSection(
                uiState = uiState,
                isSelf = isSelf,
                onEditProfileClick = onEditProfileClick,
                onRecordClick = onRecordClick,
            )
        }

        // 3. 极简作品 Tab 栏
        item(span = { GridItemSpan(3) }) {
            ProfileTabs(
                selectedTab = uiState.selectedTab,
                worksCount = uiState.works.size,
                likedCount = uiState.likedVideos.size,
                bookmarksCount = uiState.bookmarkedVideos.size,
                onTabSelect = viewModel::onTabSelect,
            )
        }

        // 4. 媒体作品列表网格
        if (uiState.isLoading && currentList.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color(0xFFE5C384), strokeWidth = 2.dp)
                }
            }
        } else if (currentList.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                ) {
                    Text(
                        text = "暂无作品",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFAF6EE).copy(alpha = 0.4f),
                    )
                    if (isSelf) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRecordClick,
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF181716),
                                    contentColor = Color(0xFFE5C384),
                                ),
                            border =
                                ButtonDefaults.outlinedButtonBorder().copy(
                                    brush =
                                        androidx.compose.ui.graphics
                                            .SolidColor(Color(0xFFE5C384).copy(alpha = 0.5f)),
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0xFFE5C384),
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "去拍摄",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
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
 * 极简主页顶部栏。
 */
@Composable
private fun ProfileTopBar(
    isSelf: Boolean,
    nickname: String,
    onBackClick: (() -> Unit)?,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFFFAF6EE),
                    modifier = Modifier.size(22.dp),
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Text(
            text = if (isSelf) "我" else nickname,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFAF6EE),
        )

        if (isSelf) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color(0xFFFAF6EE),
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

/**
 * 用户核心资料与操作按钮区域。
 */
@Composable
private fun ProfileHeaderSection(
    uiState: ProfileUiState,
    isSelf: Boolean,
    onEditProfileClick: () -> Unit,
    onRecordClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. 头像
        AsyncImage(
            model = uiState.user?.avatarUrl,
            contentDescription = uiState.user?.nickname,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF181716))
                    .border(1.dp, Color(0xFFE5C384).copy(alpha = 0.5f), CircleShape),
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 昵称与简介
        Text(
            text = uiState.user?.nickname ?: "用户",
            fontSize = 18.sp,
            color = Color(0xFFFAF6EE),
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = uiState.user?.bio ?: "分享生活的美好瞬间 ✨",
            fontSize = 12.sp,
            color = Color(0xFFFAF6EE).copy(alpha = 0.6f),
            lineHeight = 16.sp,
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. 核心统计数据（获赞 · 关注 · 粉丝）- 留白排版，无厚重边框
        Row(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(count = formatNumber(uiState.totalLikedCount), label = "获赞")
            StatItem(count = "${uiState.followingCount}", label = "关注")
            StatItem(count = formatNumber(uiState.followerCount), label = "粉丝")
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. 核心功能按钮
        if (isSelf) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 编辑资料
                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFAF6EE),
                        ),
                    border =
                        ButtonDefaults.outlinedButtonBorder().copy(
                            brush =
                                androidx.compose.ui.graphics
                                    .SolidColor(Color(0xFF33302C)),
                        ),
                ) {
                    Text(
                        text = "编辑资料",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 核心拍摄入口
                Button(
                    onClick = onRecordClick,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5C384),
                            contentColor = Color(0xFF0E0D0C),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "拍摄",
                        tint = Color(0xFF0E0D0C),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "拍摄",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        } else {
            Button(
                onClick = onEditProfileClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                shape = RoundedCornerShape(19.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5C384),
                        contentColor = Color(0xFF0E0D0C),
                    ),
            ) {
                Text(
                    text = if (uiState.user?.isFollowing == true) "已关注" else "关注",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * 统计数据项。
 */
@Composable
private fun StatItem(
    count: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 16.sp,
            color = Color(0xFFFAF6EE),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFFAF6EE).copy(alpha = 0.5f),
        )
    }
}

/**
 * 作品/喜欢/收藏 Tab 栏。
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
            HorizontalDivider(color = Color(0xFFFAF6EE).copy(alpha = 0.08f))
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
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFFFAF6EE) else Color(0xFFFAF6EE).copy(alpha = 0.45f),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

/**
 * 3 列媒体卡片。
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
                .background(Color(0xFF141312))
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // 底部播放点赞数
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
