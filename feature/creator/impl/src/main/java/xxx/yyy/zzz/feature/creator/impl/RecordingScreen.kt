package xxx.yyy.zzz.feature.creator.impl

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 视频录制/拍摄取景页 Composable。
 */
@Composable
fun RecordingScreen(
    uiState: CreatorUiState,
    onToggleRecord: () -> Unit,
    onSpeedSelect: (Float) -> Unit,
    onFlipCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onNextStep: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 录制中呼吸动效
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isRecording) 1.15f else 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 600),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulse_scale",
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        // 取景器模拟渐变背景（真实场景接入 CameraX / VideoKit 取景）
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color.Black),
                        ),
                    ),
        )

        // 顶部操作栏与进度条
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // 录制进度条
            LinearProgressIndicator(
                progress = { uiState.recordProgress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                color = Color(0xFFFF2C55),
                trackColor = Color.White.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 顶栏按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                // 右侧功能图标列
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onToggleFlash) {
                        Icon(
                            imageVector = if (uiState.isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "闪光灯",
                            tint = if (uiState.isFlashOn) Color.Yellow else Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    IconButton(onClick = onFlipCamera) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "翻转镜头",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }

        // 底部控制区（倍速选择 + 拍摄大红按钮 + 下一步）
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 录制时长提示
            if (uiState.recordedDurationSeconds > 0) {
                Text(
                    text = "${uiState.recordedDurationSeconds}s / 15s",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 倍速切换栏
            Row(
                modifier =
                    Modifier
                        .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(0.5f to "0.5x", 1.0f to "1x", 2.0f to "2x", 3.0f to "3x").forEach { (speed, label) ->
                    val isSelected = uiState.selectedSpeed == speed
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { onSpeedSelect(speed) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 底部快门控制行
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 相册导入
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { /* 打开相册选择 */ },
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "相册",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "相册", color = Color.White, fontSize = 11.sp)
                }

                // 快门录制大红圆环按钮
                Box(
                    modifier =
                        Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clickable(onClick = onToggleRecord),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .scale(pulseScale)
                                .clip(if (uiState.isRecording) RoundedCornerShape(8.dp) else CircleShape)
                                .background(Color(0xFFFF2C55)),
                    )
                }

                // 下一步完成按钮
                if (uiState.recordProgress > 0.05f) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2C55))
                                .clickable(onClick = onNextStep),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "完成",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }
    }
}
