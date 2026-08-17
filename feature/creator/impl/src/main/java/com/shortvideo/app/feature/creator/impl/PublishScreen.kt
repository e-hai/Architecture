package com.shortvideo.app.feature.creator.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/**
 * 视频发布与信息填写页 Composable。
 */
@Composable
fun PublishScreen(
    uiState: CreatorUiState,
    onTitleChange: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onTogglePublic: (Boolean) -> Unit,
    onPublish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val candidateTags = listOf("生活", "摄影", "日常", "旅行", "治愈系", "Compose", "Kotlin")

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        // 顶栏
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White,
                )
            }
            Text(
                text = "发布动态",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "存草稿",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onBack() },
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // 内容填写区
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(16.dp),
        ) {
            // 封面与标题输入
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                OutlinedTextField(
                    value = uiState.publishTitle,
                    onValueChange = onTitleChange,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(110.dp),
                    placeholder = {
                        Text(
                            text = "添加合适的标题和描述，让更多人看到你的作品...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                    },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 封面预览图
                AsyncImage(
                    model = "https://picsum.photos/300/400?random=creator_preview",
                    contentDescription = "封面预览",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(width = 80.dp, height = 110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 热门话题选择
            Text(
                text = "添加话题标签",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(candidateTags) { tag ->
                    val isSelected = uiState.selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTagToggle(tag) },
                        label = { Text("#$tag", fontSize = 12.sp) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = Color(0xFFFF2C55),
                                selectedLabelColor = Color.White,
                            ),
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // 权限设置行：公开可见
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onTogglePublic(!uiState.isPublic) }
                        .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.isPublic) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = "可见范围",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "谁可以看",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
                Text(
                    text = if (uiState.isPublic) "公开 · 所有人可见" else "私密 · 仅自己可见",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            // 权限设置行：允许评论
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "允许他人评论",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
                Switch(
                    checked = uiState.allowComments,
                    onCheckedChange = { /* 切换允许评论 */ },
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF2C55),
                        ),
                )
            }
        }

        // 底部发布按钮
        Button(
            onClick = onPublish,
            enabled = !uiState.isPublishing,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2C55),
                    contentColor = Color.White,
                ),
        ) {
            if (uiState.isPublishing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = "立即发布",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
