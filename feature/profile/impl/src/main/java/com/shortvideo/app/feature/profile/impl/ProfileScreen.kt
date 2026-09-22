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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * 遵循极简与留白原则：
 * 1. 标题栏左侧仅保留拍摄图标按钮（未包含冗余文案）。
 * 2. 移除个人主页中的获赞、关注、粉丝统计。
 * 3. 将编辑资料收纳至设置页。
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

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showEditProfileSheet by remember { mutableStateOf(false) }

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
        // 1. 标题栏（左侧拍摄图标，右侧设置图标）
        item(span = { GridItemSpan(3) }) {
            ProfileTopBar(
                isSelf = isSelf,
                nickname = uiState.user?.nickname.orEmpty(),
                onBackClick = onBackClick,
                onRecordClick = onRecordClick,
                onSettingsClick = {
                    onSettingsClick()
                    showSettingsSheet = true
                },
            )
        }

        // 2. 核心用户资料（头像、昵称、签名）
        item(span = { GridItemSpan(3) }) {
            ProfileHeaderSection(
                uiState = uiState,
                isSelf = isSelf,
                onFollowClick = onEditProfileClick,
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

    // 设置底部抽屉（包含编辑资料核心入口）
    if (showSettingsSheet) {
        SettingsBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            onEditProfileClick = {
                showSettingsSheet = false
                showEditProfileSheet = true
                onEditProfileClick()
            },
        )
    }

    // 编辑资料底部抽屉
    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            initialNickname = uiState.user?.nickname.orEmpty(),
            initialBio = uiState.user?.bio.orEmpty(),
            avatarUrl = uiState.user?.avatarUrl.orEmpty(),
            onDismissRequest = { showEditProfileSheet = false },
            onSave = { _, _ ->
                showEditProfileSheet = false
            },
        )
    }
}

/**
 * 极简主页顶部栏。
 * 标题栏左侧为拍摄图标按钮，右侧为设置按钮。
 */
@Composable
private fun ProfileTopBar(
    isSelf: Boolean,
    nickname: String,
    onBackClick: (() -> Unit)?,
    onRecordClick: () -> Unit,
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
        // 左侧：若查看他人展示返回按钮，若查看自己展示拍摄图标按钮
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFFFAF6EE),
                    modifier = Modifier.size(22.dp),
                )
            }
        } else if (isSelf) {
            IconButton(onClick = onRecordClick) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "拍摄",
                    tint = Color(0xFFFAF6EE),
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        // 中间：标题
        Text(
            text = if (isSelf) "我" else nickname,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFAF6EE),
        )

        // 右侧：若自己展示设置按钮
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
 * 用户核心资料区域（仅头像、昵称、签名，无统计项与操作按钮干扰）。
 */
@Composable
private fun ProfileHeaderSection(
    uiState: ProfileUiState,
    isSelf: Boolean,
    onFollowClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 头像
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

        // 昵称
        Text(
            text = uiState.user?.nickname ?: "用户",
            fontSize = 18.sp,
            color = Color(0xFFFAF6EE),
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 个人简介
        Text(
            text = uiState.user?.bio ?: "分享生活的美好瞬间 ✨",
            fontSize = 12.sp,
            color = Color(0xFFFAF6EE).copy(alpha = 0.6f),
            lineHeight = 16.sp,
        )

        // 他人主页显示关注按钮
        if (!isSelf) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onFollowClick,
                modifier =
                    Modifier
                        .fillMaxWidth(0.42f)
                        .height(36.dp),
                shape = RoundedCornerShape(18.dp),
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

        Spacer(modifier = Modifier.height(10.dp))
    }
}

/**
 * 设置底部抽屉。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBottomSheet(
    onDismissRequest: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF161514),
        contentColor = Color(0xFFFAF6EE),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
        ) {
            // 顶栏
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "设置",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAF6EE),
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color(0xFFFAF6EE).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFFAF6EE).copy(alpha = 0.06f))

            // 编辑资料核心入口
            SettingsItemRow(
                icon = Icons.Default.Edit,
                title = "编辑资料",
                subtitle = "修改头像、昵称与个人签名",
                onClick = onEditProfileClick,
            )

            // 清除缓存
            SettingsItemRow(
                icon = Icons.Default.DeleteOutline,
                title = "清除缓存",
                subtitle = "已占用 32.5 MB",
                onClick = {},
            )

            // 关于
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "关于应用",
                subtitle = "版本 1.0.0",
                onClick = {},
            )

            // 退出登录
            SettingsItemRow(
                icon = Icons.Default.Logout,
                title = "退出登录",
                titleColor = Color(0xFFFF5252),
                showArrow = false,
                onClick = onDismissRequest,
            )
        }
    }
}

/**
 * 设置行组件。
 */
@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color(0xFFFAF6EE),
    showArrow: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == Color(0xFFFAF6EE)) Color(0xFFE5C384) else titleColor,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFFFAF6EE).copy(alpha = 0.45f),
                )
            }
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFFAF6EE).copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * 编辑资料抽屉。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    initialNickname: String,
    initialBio: String,
    avatarUrl: String,
    onDismissRequest: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nickname by remember { mutableStateOf(initialNickname) }
    var bio by remember { mutableStateOf(initialBio) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF161514),
        contentColor = Color(0xFFFAF6EE),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 顶栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "编辑资料",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAF6EE),
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color(0xFFFAF6EE).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 头像
            AsyncImage(
                model = avatarUrl,
                contentDescription = "修改头像",
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22201E))
                        .border(1.dp, Color(0xFFE5C384).copy(alpha = 0.5f), CircleShape),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 昵称输入
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称", color = Color(0xFFFAF6EE).copy(alpha = 0.6f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFAF6EE),
                        unfocusedTextColor = Color(0xFFFAF6EE),
                        focusedBorderColor = Color(0xFFE5C384),
                        unfocusedBorderColor = Color(0xFFFAF6EE).copy(alpha = 0.2f),
                    ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 简介输入
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("个人简介", color = Color(0xFFFAF6EE).copy(alpha = 0.6f)) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFAF6EE),
                        unfocusedTextColor = Color(0xFFFAF6EE),
                        focusedBorderColor = Color(0xFFE5C384),
                        unfocusedBorderColor = Color(0xFFFAF6EE).copy(alpha = 0.2f),
                    ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = { onSave(nickname, bio) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5C384),
                        contentColor = Color(0xFF0E0D0C),
                    ),
            ) {
                Text(
                    text = "保存修改",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
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
