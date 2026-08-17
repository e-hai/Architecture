package com.shortvideo.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.shortvideo.app.core.navigation.NavigationState
import com.shortvideo.app.core.navigation.Navigator
import com.shortvideo.app.core.navigation.toEntries
import com.shortvideo.app.feature.comment.impl.CommentBottomSheet
import com.shortvideo.app.feature.creator.api.CreatorNavKey
import com.shortvideo.app.feature.creator.impl.CreatorScreen
import com.shortvideo.app.feature.discover.api.DiscoverNavKey
import com.shortvideo.app.feature.discover.impl.DiscoverScreen
import com.shortvideo.app.feature.feed.api.FeedNavKey
import com.shortvideo.app.feature.feed.impl.FeedScreen
import com.shortvideo.app.feature.profile.api.ProfileNavKey
import com.shortvideo.app.feature.profile.impl.ProfileScreen

/**
 * 应用主导航图（包含 3-Tab 底部导航、全屏拍摄发布与各页面路由）。
 *
 * @param navigationState 全局导航状态机
 * @param navigator 导航控制器
 */
@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val currentKey = navigationState.currentTopLevelKey
    var activeCommentVideoId by remember { mutableStateOf<String?>(null) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        when (key) {
            is FeedNavKey -> {
                NavEntry(key) {
                    FeedScreen(
                        onAuthorClick = { authorId -> navigator.navigate(ProfileNavKey(userId = authorId)) },
                        onCommentClick = { videoId -> activeCommentVideoId = videoId },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            is DiscoverNavKey -> {
                NavEntry(key) {
                    DiscoverScreen(
                        onVideoClick = { videoId -> navigator.navigate(FeedNavKey(initialVideoId = videoId)) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            is ProfileNavKey -> {
                NavEntry(key) {
                    ProfileScreen(
                        userId = key.userId,
                        onVideoClick = { videoId -> navigator.navigate(FeedNavKey(initialVideoId = videoId)) },
                        onEditProfileClick = { /* 打开编辑资料页 */ },
                        onSettingsClick = { /* 打开设置页 */ },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            is CreatorNavKey -> {
                NavEntry(key) {
                    CreatorScreen(
                        onClose = { navigator.goBack() },
                        onPublishSuccess = { navigator.goBack() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            else -> {
                NavEntry(key) {
                    PlaceholderScreen(title = "未知页面", desc = "Key: $key")
                }
            }
        }
    }

    val isTopLevelTab =
        currentKey is FeedNavKey || currentKey is DiscoverNavKey || currentKey is ProfileNavKey

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (isTopLevelTab) {
                AppBottomNavBar(
                    currentKey = currentKey,
                    onTabSelect = { targetKey ->
                        navigator.navigate(targetKey)
                    },
                )
            }
        },
        containerColor = Color.Black,
    ) { innerPadding ->
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
        )

        // 评论底部弹窗
        activeCommentVideoId?.let { videoId ->
            CommentBottomSheet(
                videoId = videoId,
                onDismissRequest = { activeCommentVideoId = null },
            )
        }
    }
}

/**
 * 3-Tab 底部导航栏组件。
 */
@Composable
private fun AppBottomNavBar(
    currentKey: NavKey,
    onTabSelect: (NavKey) -> Unit,
) {
    val tabs =
        listOf(
            TabItem(
                key = FeedNavKey(),
                label = "首页",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            TabItem(
                key = DiscoverNavKey(),
                label = "发现",
                selectedIcon = Icons.Filled.Explore,
                unselectedIcon = Icons.Outlined.Explore,
            ),
            TabItem(
                key = ProfileNavKey(),
                label = "我",
                selectedIcon = Icons.Filled.AccountCircle,
                unselectedIcon = Icons.Outlined.AccountCircle,
            ),
        )

    NavigationBar(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        containerColor = Color.Black.copy(alpha = 0.9f),
        contentColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        tabs.forEach { tab ->
            val isSelected =
                when (tab.key) {
                    is FeedNavKey -> currentKey is FeedNavKey
                    is DiscoverNavKey -> currentKey is DiscoverNavKey
                    is ProfileNavKey -> currentKey is ProfileNavKey
                    else -> currentKey == tab.key
                }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(tab.key) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                        indicatorColor = Color.Transparent,
                    ),
            )
        }
    }
}

private data class TabItem(
    val key: NavKey,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
private fun PlaceholderScreen(
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}
