package xxx.yyy.zzz.feature.debug.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import xxx.yyy.zzz.core.log.CoreLog
import xxx.yyy.zzz.core.model.AppConfig
import xxx.yyy.zzz.core.navigation.BackHandler
import xxx.yyy.zzz.core.ui.generated.resources.Res
import xxx.yyy.zzz.core.ui.generated.resources.back
import xxx.yyy.zzz.core.ui.generated.resources.clear_log
import xxx.yyy.zzz.core.ui.generated.resources.debug_console_title
import xxx.yyy.zzz.feature.debug.api.DebugCategory
import xxx.yyy.zzz.feature.debug.api.DebugSection

private const val MAX_CONSOLE_LOGS = 100
private const val CONSOLE_CONTAINER_COLOR = 0xFF1E1E1E
private const val CONSOLE_MUTED_COLOR = 0xFF888888
private const val CONSOLE_DIVIDER_COLOR = 0xFF333333
private const val CONSOLE_PLACEHOLDER_COLOR = 0xFF555555
private const val CONSOLE_LOG_TEXT_COLOR = 0xFF00FF66

/**
 * 开发者调试面板（DebugScreen）。
 * 作为纯容器通过 Koin 收集各模块注入的 [DebugSection]，
 * 支持热插拔增删 Kit 而不破坏主调试界面编译。
 */
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val koin = getKoin()
    val appConfig: AppConfig = koinInject()
    val sectionsByCategory =
        remember(koin) {
            koin
                .getAll<DebugSection>()
                .distinctBy { it.id }
                .sortedBy { it.order }
                .groupBy { it.category }
        }

    // 屏幕实时日志回显列表（轻量级即时反馈）
    val consoleLogs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    fun emitLog(
        tag: String,
        message: String,
        level: String = "I",
    ) {
        val displayEntry = "[$tag][$level] $message"
        consoleLogs.add(displayEntry)
        if (consoleLogs.size > MAX_CONSOLE_LOGS) consoleLogs.removeAt(0)

        when (level.uppercase()) {
            "V", "VERBOSE" -> CoreLog.v(tag, message)
            "D", "DEBUG" -> CoreLog.d(tag, message)
            "W", "WARN" -> CoreLog.w(tag, message)
            "E", "ERROR" -> CoreLog.e(tag, message)
            "WTF" -> CoreLog.wtf(tag, message)
            else -> CoreLog.i(tag, message)
        }
    }

    LaunchedEffect(consoleLogs.size) {
        if (consoleLogs.isNotEmpty()) {
            listState.animateScrollToItem(consoleLogs.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DebugTopBar(onBack = onBack, onClearLogs = { consoleLogs.clear() }) }
        item { DebugConsoleCard(consoleLogs = consoleLogs.toImmutableList(), listState = listState) }
        item { AppConfigCard(appConfig = appConfig) }

        DebugCategory.entries.forEach { category ->
            val categorySections = sectionsByCategory[category].orEmpty()
            if (categorySections.isNotEmpty()) {
                item(key = category.name) {
                    DebugCategoryCard(
                        category = category,
                        sections = categorySections.toImmutableList(),
                        emitLog = ::emitLog,
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugTopBar(
    onBack: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onBack) {
            Text(stringResource(Res.string.back))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.debug_console_title),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = onClearLogs) {
            Text(stringResource(Res.string.clear_log))
        }
    }
}

@Composable
private fun DebugConsoleCard(
    consoleLogs: ImmutableList<String>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(CONSOLE_CONTAINER_COLOR)),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "轻量回显 (${consoleLogs.size})",
                    color = Color(CONSOLE_MUTED_COLOR),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Button(
                    onClick = { CoreLog.showLogUi() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("打开 LogKit 原生控制台 ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = Color(CONSOLE_DIVIDER_COLOR), modifier = Modifier.padding(vertical = 4.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (consoleLogs.isEmpty()) {
                    item {
                        Text(
                            text = "点击下方按钮触发各 Kit 接口联调（自动同步写入 LogKit 磁盘）...",
                            color = Color(CONSOLE_PLACEHOLDER_COLOR),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
                items(consoleLogs.size) { index ->
                    Text(
                        text = consoleLogs[index],
                        color = Color(CONSOLE_LOG_TEXT_COLOR),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppConfigCard(
    appConfig: AppConfig,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("1. 运行环境与 App 配置", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text("• 运行环境: ${appConfig.env}", style = MaterialTheme.typography.bodyMedium)
            Text("• 调试模式 (isDebug): ${appConfig.isDebug}", style = MaterialTheme.typography.bodyMedium)
            Text("• 服务端基地址: ${appConfig.baseUrl}", style = MaterialTheme.typography.bodyMedium)
            Text("• 应用版本: ${appConfig.versionName} (${appConfig.versionCode})", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DebugCategoryCard(
    category: DebugCategory,
    sections: ImmutableList<DebugSection>,
    emitLog: (tag: String, message: String, level: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(category.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                section.Content(emitLog)
            }
        }
    }
}
